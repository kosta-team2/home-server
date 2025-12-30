package com.home.infrastructure.batch.tradealarm.processor;

import java.nio.charset.StandardCharsets;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.home.infrastructure.batch.tradealarm.dto.MailTargetRow;
import com.home.infrastructure.batch.tradealarm.exception.NonRetryableMailException;
import com.home.infrastructure.batch.tradealarm.exception.RetryableMailException;

public class MailSendProcessor implements ItemProcessor<MailTargetRow, MailTargetRow> {

	private final JdbcTemplate jdbc;
	private final JavaMailSender mailSender;
	private final String fromAddress;
	private final String fromName;

	public MailSendProcessor(JdbcTemplate jdbc, JavaMailSender mailSender, String fromAddress, String fromName) {
		this.jdbc = jdbc;
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
		this.fromName = fromName;
	}

	@Override
	public MailTargetRow process(MailTargetRow item) {
		// 선점(동시 실행 방지 목적). 단일 실행이면 없어도 됨.
		int claimed = jdbc.update("""
            update mail_target
               set status = 'PROCESSING'
             where id = ?
               and status in ('PENDING','FAILED')
            """, item.id());

		if (claimed == 0) {
			// 이미 다른 실행이 가져갔다고 보고 필터링
			return null;
		}

		try {
			sendMail(item);

			var stepContext = StepSynchronizationManager.getContext();
			if (stepContext != null) {
				var jobCtx = stepContext
					.getStepExecution()
					.getJobExecution()
					.getExecutionContext();

				jobCtx.putLong(
					"mail.sent",
					jobCtx.getLong("mail.sent", 0L) + 1
				);
			}

			return item;

		} catch (MailAuthenticationException e) {
			// 설정 문제: 보통 배치 전체 실패가 맞음(재시도/스킵보다)
			throw e;

		} catch (MailSendException e) {
			// 일시적인 네트워크/SMTP 문제 가능성이 높아 retry 대상
			throw new RetryableMailException("SMTP 전송 실패(재시도 대상): " + safeMsg(e), e);

		} catch (Exception e) {
			// 나머지는 스킵 대상(주소/메시지 구성 등)
			throw new NonRetryableMailException("메일 전송 실패(스킵 대상): " + safeMsg(e), e);
		}
	}

	private void sendMail(MailTargetRow row) throws Exception {
		MimeMessage mime = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());

		helper.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
		helper.setTo(row.email());
		helper.setSubject("[거래 알림] 관심 단지 거래 업데이트");

		String body = """
                안녕하세요.

                관심 단지로 설정한 [%s]에 새로운 거래 정보가 등록되었습니다.
                자세한 정보는 사이트에 접속해서 확인해주세요!
                "https://www.homesearch.world/"

                감사합니다.
                """.formatted(row.complexName());

		helper.setText(body, false);
		mailSender.send(mime);
	}

	private String safeMsg(Throwable t) {
		return (t.getMessage() == null) ? t.getClass().getSimpleName() : t.getMessage();
	}
}

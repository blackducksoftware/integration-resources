@Grab(group = 'com.sun.mail', module = 'javax.mail', version = '1.6.2')
@Grab(group = 'org.jsoup', module = 'jsoup', version = '1.12.1')

import org.jsoup.Jsoup

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class Emailer {
    void sendEmail(String to, String from, String subject, String host, String html) {
        String text = Jsoup.parse(html).text()

        Properties properties = new Properties()
        properties.setProperty('mail.smtp.host', host)

        Session session = Session.getDefaultInstance(properties)

        try {
            def emailContent = new MimeMultipart('alternative')

            // add from low fidelity to high fidelity
            addTextBody(text, emailContent)
            addHtmlBody(html, emailContent)

            def emailBody = new MimeBodyPart()
            emailBody.setContent(emailContent)

            def fullEmailMessage = new MimeMultipart('mixed')
            fullEmailMessage.addBodyPart(emailBody)

            // if we ever wanted to add attachments, they'd go here, attached to the fullEmailMessage

            MimeMessage message = new MimeMessage(session)
            message.setFrom(new InternetAddress(from))
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))
            message.setSubject(subject)
            message.setContent(fullEmailMessage)

            Transport.send(message)
            System.out.println('Sent message successfully....')
        } catch (MessagingException mex) {
            mex.printStackTrace()
        }
    }

    private void addHtmlBody(String html, MimeMultipart emailContent) {
        def fullHtmlMessage = new MimeMultipart('related')

        def htmlContent = new MimeBodyPart()
        htmlContent.setContent(html, 'text/html; charset=utf-8')
        fullHtmlMessage.addBodyPart(htmlContent)

        // if we ever wanted to add images, they'd go here, attached to the fullHtmlMessage

        def htmlBody = new MimeBodyPart()
        htmlBody.setContent(fullHtmlMessage)
        emailContent.addBodyPart(htmlBody)
    }

    private void addTextBody(String text, MimeMultipart emailContent) {
        def textBody = new MimeBodyPart()
        textBody.setText(text, 'utf-8')
        emailContent.addBodyPart(textBody)
    }

}

/*
 * activity
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.activity.report

import org.jsoup.Jsoup
import org.springframework.stereotype.Component

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Component
class EmailReporter extends Reporter<EmailReportContext> {
    void produceReport(String contents, EmailReportContext reportContext) {
        String text = Jsoup.parse(contents).text()

        Properties properties = new Properties()
        properties.setProperty('mail.smtp.host', reportContext.host)

        Session session = Session.getDefaultInstance(properties)

        try {
            def emailContent = new MimeMultipart('alternative')

            // add from low fidelity to high fidelity
            addTextBody(text, emailContent)
            addHtmlBody(contents, emailContent)

            def emailBody = new MimeBodyPart()
            emailBody.setContent(emailContent)

            def fullEmailMessage = new MimeMultipart('mixed')
            fullEmailMessage.addBodyPart(emailBody)

            // if we ever wanted to add attachments, they'd go here, attached to the fullEmailMessage

            MimeMessage message = new MimeMessage(session)
            message.setFrom(new InternetAddress(reportContext.from))
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(reportContext.to))
            message.setSubject(reportContext.subject)
            message.setContent(fullEmailMessage)

            Transport.send(message)
            System.out.println('Sent message successfully....')
        } catch (MessagingException e) {
            e.printStackTrace()
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

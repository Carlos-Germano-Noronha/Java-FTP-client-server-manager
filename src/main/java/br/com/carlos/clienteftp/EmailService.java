package br.com.carlos.clienteftp;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import java.io.File;


public class EmailService {

    // --- CONFIGURE SEUS DADOS AQUI ---
    private final String remetente = "carlospg199@gmail.com";
    private final String senhaRemetente = "lrzz xsgu leuf njnb";
    private final String smtpHost = "smtp.gmail.com";
    private final String smtpPort = "587";
    // ------------------------------------

    /**
     * Envia um e-mail com um corpo de texto e um arquivo em anexo.
     * @param destinatario O e-mail do destinatário.
     * @param assunto O assunto do e-mail.
     * @param corpoTexto O texto principal do e-mail.
     * @param caminhoAnexo O caminho completo do arquivo a ser anexado.
     * @return true se o e-mail for enviado com sucesso, false caso contrário.
     */
    public void emailConfig(String destinatario, String assunto, String corpoTexto, String caminhoAnexo) throws EmailException {
        File anexo = new File(caminhoAnexo);
        if (!anexo.exists() || anexo.isDirectory()) {
            throw new EmailException("Caminho do anexo é inválido: " + caminhoAnexo, null);
        }
        //  Configura as propriedades do servidor SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", this.smtpHost);
        props.put("mail.smtp.port", this.smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Habilita conexão segura TLS

        //  Criar uma sessão com autenticação
        Session sessao = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remetente, senhaRemetente);
            }
        });

        try {
            // Criar a mensagem (MimeMessage)
            Message mensagem = new MimeMessage(sessao);
            mensagem.setFrom(new InternetAddress(this.remetente));
            mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensagem.setSubject(assunto);

            // Criar o corpo da mensagem (parte de texto)
            MimeBodyPart parteTexto = new MimeBodyPart();
            parteTexto.setText(corpoTexto);

            // Criar a parte do anexo
            MimeBodyPart parteAnexo = new MimeBodyPart();
            FileDataSource fonteDados = new FileDataSource(caminhoAnexo);
            parteAnexo.setDataHandler(new DataHandler(fonteDados));
            parteAnexo.setFileName(fonteDados.getName()); // Define o nome do anexo

            // Juntar as partes em um "Multipart"
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(parteTexto);
            multipart.addBodyPart(parteAnexo);

            // Definir o conteúdo da mensagem como o multipart
            mensagem.setContent(multipart);

            // Enviar a mensagem
            Transport.send(mensagem);

        } catch (MessagingException e) {
            throw new EmailException("Erro ao enviar e-mail: " + e.getMessage(), e);
        }
    }

}
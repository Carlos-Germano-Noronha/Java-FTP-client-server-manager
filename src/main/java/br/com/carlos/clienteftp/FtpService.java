package br.com.carlos.clienteftp;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import java.io.*;

public class FtpService {

    private final FTPClient ftpClient;

    public FtpService() {
        this.ftpClient = new FTPClient();
    }
    //conecta ao servidor FTP e retorna o sucesso ou erro
    public void conectar(String host, String user, String pass) throws FtpException {
        try {
            ftpClient.connect(host);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new FtpException("Falha na conexão. Código do servidor: " + replyCode, null);
            }
            if (!ftpClient.login(user, pass)) {
                throw new FtpException("Usuário ou senha inválidos.", null);
            }
            ftpClient.enterLocalPassiveMode();
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao conectar: " + e.getMessage(), e);
        }
    }
    // Lista Arquivos
    public FTPFile[] listFiles(String path) throws FtpException {
        try {
            return ftpClient.listFiles(path);
        } catch (IOException e) {
            throw new FtpException("Erro ao listar arquivos: " + e.getMessage(), e);
        }
    }
    //Upa Arquivos
    public void uploadFile(String localArquivo, String nomeArquivo) throws FtpException {
        try (InputStream inputStream = new FileInputStream(localArquivo)) {
            if (!ftpClient.storeFile(nomeArquivo, inputStream)) {
                throw new FtpException("Falha ao enviar o arquivo (verifique permissões).", null);
            }
        } catch (FileNotFoundException e) {
            throw new FtpException("Arquivo local não encontrado: " + localArquivo, e);
        } catch (IOException e) {
            throw new FtpException("Erro de I/O durante o upload: " + e.getMessage(), e);
        }
    }
    //Baixa Arquivos
    public void baixaArquivo(String nomeArquivo, String localArquivo) throws FtpException {
        try (OutputStream outputStream = new FileOutputStream(localArquivo)) {
            if (!ftpClient.retrieveFile(nomeArquivo, outputStream)) {
                throw new FtpException("Falha ao baixar o arquivo (verifique se o nome está correto).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O durante o download: " + e.getMessage(), e);
        }
    }
    // Cria Diretório
    public void criaDiretorio(String nomeDiretorio) throws FtpException {
        try {
            if (!ftpClient.makeDirectory(nomeDiretorio)) {
                throw new FtpException("Falha ao criar diretório (pode já existir ou falta de permissão).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao criar diretório: " + e.getMessage(), e);
        }
    }
    //Delera Diretório
    public void removeDiretorio(String nomeDiretorio) throws FtpException {
        try {
            if (!ftpClient.removeDirectory(nomeDiretorio)) {
                throw new FtpException("Falha ao remover diretório (verifique se está vazio e se há permissão).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao remover diretório: " + e.getMessage(), e);
        }
    }
    //deleta arquivos
    public void deleteArquivo(String pastaArquivo) throws FtpException {
        try {
            if (!ftpClient.deleteFile(pastaArquivo)) {
                throw new FtpException("Falha ao remover arquivo (verifique se existe e se há permissão).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao remover arquivo: " + e.getMessage(), e);
        }
    }
    //Renomeia arquivos Diretórios
    public void renomear(String nomeAGE, String newNome) throws FtpException {
        try {
            if (!ftpClient.rename(nomeAGE, newNome)) {
                throw new FtpException("Falha ao renomear (verifique se o item de origem existe).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao renomear: " + e.getMessage(), e);
        }
    }
    //muda Diretório
    public void numdarDiretorio(String pasta) throws FtpException {
        try {
            if (!ftpClient.changeWorkingDirectory(pasta)) {
                throw new FtpException("Falha ao mudar de diretório (verifique se existe).", null);
            }
        } catch (IOException e) {
            throw new FtpException("Erro de I/O ao mudar de diretório: " + e.getMessage(), e);
        }
    }
    //Gera Relatório
    public String getListaCompletaDiretorios() throws FtpException {
        StringBuilder reportBuilder = new StringBuilder("Relatório de Conteúdo do Servidor FTP:\n");
        try {
            listaDirectoryRecursivo("/", 0, reportBuilder);
        } catch (IOException e) {
            throw new FtpException("Não foi possível gerar o relatório completo: " + e.getMessage(), e);
        }
        return reportBuilder.toString();
    }
    //lista os diretórios de forma recursiva para o email
    private void listaDirectoryRecursivo(String parentDir, int indentLevel, StringBuilder reportBuilder) throws IOException {
        String indent = "  ".repeat(indentLevel);

        // Entra no diretório especificado
        if (!ftpClient.changeWorkingDirectory(parentDir)) {
            return;
        }

        FTPFile[] files = ftpClient.listFiles();
        if (files != null && files.length > 0) {
            for (FTPFile file : files) {
                if (file.isDirectory()) {
                    reportBuilder.append(indent).append("DIR:  ").append(file.getName()).append("/\n");
                    listaDirectoryRecursivo(file.getName(), indentLevel + 1, reportBuilder);
                } else {
                    reportBuilder.append(indent).append("FILE: ").append(file.getName()).append("\n");
                }
            }
        }

        ftpClient.changeToParentDirectory();
    }
    //disconecta do servidor
    public void desconectar() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
            }
        }
    }
    //chama o recusrivo de contagem para saber o padrão home do servidor
    public int getTotalDiretorio() throws FtpException{
        try {
            String atualDiretorio = ftpClient.printWorkingDirectory();
            int contagem = contagemDiretorioRecursivo("/");
            ftpClient.changeWorkingDirectory(atualDiretorio);
            return contagem;
        } catch (IOException e){
            throw new FtpException("Erro ao contar diretorios do servidor", e);
        }
    }
    private int contagemDiretorioRecursivo(String aturalDiretorio) throws IOException{
        int contagem = 0;
        if(!ftpClient.changeWorkingDirectory(aturalDiretorio)){
            return 0;
        }
        FTPFile[] files = ftpClient.listFiles();
        if (files != null){
            for (FTPFile file : files ){
                if (file.isDirectory() && !file.getName().equals(".") && !file.getName().equals("..")) {
                    contagem++;
                    contagem += contagemDiretorioRecursivo(file.getName());
                }
            }
        }
        ftpClient.changeToParentDirectory();
        return contagem;
    }
}
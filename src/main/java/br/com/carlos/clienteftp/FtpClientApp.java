package br.com.carlos.clienteftp;
import java.util.Scanner;
import org.apache.commons.net.ftp.FTPFile;

public class FtpClientApp {
    private final FtpService ftpService;
    private final EmailService emailService;
    private final Scanner scanner;
    private String pastaAtual;

    public FtpClientApp() {
        this.ftpService = new FtpService();
        this.emailService = new EmailService();
        this.scanner = new Scanner(System.in);
        this.pastaAtual = "/";
    }

    public void run() {
        System.out.println("--- Cliente FTP Iniciado ---");
        String host = "127.0.0.1";
        String user = "carlos";
        String pass = "2558";

        try {
            ftpService.conectar(host, user, pass);
            System.out.println("Conexão bem-sucedida!");
            menuLoop();
        } catch (FtpException e) {
            System.err.println("ERRO CRÍTICO: " + e.getMessage());
        } finally {
            System.out.println("Encerrando...");
            ftpService.desconectar();
            scanner.close();
        }
    }

    private void menuLoop() {
        int escolha = -1;
        while (escolha != 0) {
            displayMenu();
            try {
                escolha = Integer.parseInt(scanner.nextLine());
                menuEscolhas(escolha);
            } catch (NumberFormatException e) {
                System.out.println("ERRO: Por favor, digite um número válido.");
            }
        }
    }

    private void displayMenu() {
        System.out.println("\n--- MENU FTP ---");
        System.out.println("1 - Listar arquivos e pastas");
        System.out.println("2 - Fazer Upload de Arquivo");
        System.out.println("3 - Fazer Download de Arquivo");
        System.out.println("4 - Criar Diretório");
        System.out.println("5 - Remover Arquivo");
        System.out.println("6 - Remover Diretório");
        System.out.println("7 - Renomear Arquivo/Diretório");
        System.out.println("8 - Mudar de Diretório");
        System.out.println("9 - Saber posição atual");
        System.out.println("0 - Sair e Enviar Relatório");
        System.out.print("Escolha uma opção: ");
    }

    private void menuEscolhas(int escolha) {
        try {
            switch (escolha) {
                case 1: //Lista Arquivos e Diretórios
                    System.out.println("\nListando arquivos no diretório atual...");
                    FTPFile[] files = ftpService.listFiles(".");
                    if (files == null || files.length == 0) {
                        System.out.println("Nenhum arquivo encontrado.");
                    } else {
                        for (FTPFile file : files) {
                            System.out.println((file.isDirectory() ? "DIR: " : "FILE: ") + file.getName());
                        }
                    }
                    break;
                case 2: // Upload de Arquivos
                    try {
                        FTPFile[] arquivosNoDiretorio = ftpService.listFiles(".");
                        int contagem = 0;
                        if (arquivosNoDiretorio != null) {
                            for (FTPFile f : arquivosNoDiretorio) {
                                if (f.isFile()) {
                                    contagem++;
                                }
                            }
                        }
                        if (contagem >= 2) {
                            System.err.println("ERRO: O diretório atual já contém o máximo de 2 arquivos. Upload cancelado.");
                            break;
                        }
                        System.out.print("Digite o caminho do arquivo local para upload:  ex. C:/User/Documents/nomeArquivo.txt");
                        String localArquivo = scanner.nextLine();
                        System.out.print("Nome do arquivo no servidor: ");
                        String nomeArquivoServidor = scanner.nextLine();
                        ftpService.uploadFile(localArquivo, nomeArquivoServidor);
                        System.out.println("Upload concluído com sucesso!");
                    } catch (FtpException e){
                        System.out.println("ERRO NA OPERAÇÃO FTP: " + e.getMessage());
                    }
                    break;
                case 3: // Fazer Download de Arquivo
                    System.out.print("Digite o nome do arquivo no servidor para baixar: ex. exemplo.txt");
                    String arquivoParaBaixar = scanner.nextLine();
                    System.out.print("Digite o caminho completo para salvar o arquivo localmente: ex. C:/User/Documents/nomeArquivo.txt");
                    String localParaBaixar = scanner.nextLine();
                    ftpService.baixaArquivo(arquivoParaBaixar, localParaBaixar);
                    System.out.println("Download concluído com sucesso!");
                    break;
                case 4: // Criar Diretório
                    // verifica a profundidade de onde estamos em relação a pasta home "Usei IA para conseguir essa lógica, de contar profundidade confesso"
                    long profundidade = this.pastaAtual.chars().filter(ch -> ch == '/').count() -1;
                    if (profundidade >= 3){
                        System.err.println("Erro: Limite de profundiade de 03 niveis atingido." );
                        break;
                    }
                    int totalDiretorio = ftpService.getTotalDiretorio();
                    if(totalDiretorio >= 5){
                        System.out.println("Erro: limite de 05 pastas no servidor atingida");
                        break;
                    }
                    System.out.print("Digite o nome do novo diretório: ");
                    String nomeNovoDiretorio = scanner.nextLine();
                    ftpService.criaDiretorio(nomeNovoDiretorio);
                    System.out.println("Diretório criado com sucesso!");
                    break;
                case 5: // Remover Arquivo
                    System.out.print("Digite o nome do arquivo a ser removido: ex. exemplo.txt");
                    String arquivoParaDeletar = scanner.nextLine();
                    ftpService.deleteArquivo(arquivoParaDeletar);
                    System.out.println("Arquivo removido com sucesso!");
                    break;
                case 6: // Remover Diretório
                    System.out.print("Digite o nome do diretório a ser removido (deve estar vazio): ex. exemplo");
                    String diretorioParaDeletar = scanner.nextLine();
                    ftpService.removeDiretorio(diretorioParaDeletar);
                    System.out.println("Diretório removido com sucesso!");
                    break;
                case 7: // Renomear Arquivo/Diretório
                    System.out.print("Digite o nome atual do arquivo/diretório: ");
                    String nomeAtual = scanner.nextLine();
                    System.out.print("Digite o NOVO nome: ");
                    String novoNome = scanner.nextLine();
                    ftpService.renomear(nomeAtual, novoNome);
                    System.out.println("Item renomeado com sucesso!");
                    break;
                case 8: // Mudar de Diretório
                    System.out.print("Digite o nome do diretório para entrar (ou '..' para subir): ");
                    String entrarDiretorio = scanner.nextLine();
                    ftpService.numdarDiretorio(entrarDiretorio);
                    if (entrarDiretorio.equals("..")){
                        if(!this.pastaAtual.equals("/")) {
                            String pastaAtualTemporaria = this.pastaAtual.substring(0, this.pastaAtual.length() - 1);
                            int barraAnteior = pastaAtualTemporaria.lastIndexOf('/');
                            this.pastaAtual = pastaAtualTemporaria.substring(0, barraAnteior + 1);
                            }
                        } else{
                            this.pastaAtual += entrarDiretorio + "/";

                    }
                    break;
                case 9:
                    if (pastaAtual.equals("/")) {
                        System.out.println("Voce está no Inicio do servidor ");
                    } else {
                        System.out.println("Voce está na pasta: " + pastaAtual);
                    }
                    break;
                case 0:
                    enviaEmail();
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } catch (FtpException e) {
            System.err.println("ERRO NA OPERAÇÃO FTP: " + e.getMessage());
        }
    }

    private void enviaEmail() {
        try {
            System.out.println("\nGerando relatório completo...");
            String report = ftpService.getListaCompletaDiretorios();
            System.out.println(report);

            System.out.print("Digite o e-mail do destinatário: ex. user@exemplo.com");
            String destinatario = scanner.nextLine();
            System.out.print("Digite o caminho do arquivo para anexar: ex. C:/User/Documents/nomeArquivo.txt");
            String caminhoArquivoAnexo = scanner.nextLine();

            emailService.emailConfig(destinatario, "Relatório FTP", report, caminhoArquivoAnexo);

        } catch (FtpException | EmailException e) {
            System.err.println("ERRO AO GERAR/ENVIAR RELATÓRIO: " + e.getMessage());
        }
    }
}
"""
Script de Automação para Criar Conta no GitHub
Autor: Assistente AI
Data: 2024

Este script automatiza o preenchimento do formulário de registro do GitHub.
IMPORTANTE: Você precisará resolver o captcha e verificar o email manualmente.
"""

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
import time
import sys

# Configurações da conta
EMAIL = "lucasggfdd-166@yahoo.com"
USERNAME = "LucasFMO3"
PASSWORD = "30N06n86*"

def configurar_driver():
    """Configura e retorna o driver do Chrome"""
    chrome_options = Options()
    # Mantém o navegador aberto após a execução para você verificar
    chrome_options.add_experimental_option("detach", True)
    chrome_options.add_argument("--start-maximized")
    
    # Usa webdriver-manager para baixar automaticamente o ChromeDriver correto
    try:
        print("[INFO] Baixando/configurando ChromeDriver (pode demorar na primeira vez)...", flush=True)
        sys.stdout.flush()
        service = Service(ChromeDriverManager().install())
        print("[INFO] Criando instancia do Chrome...", flush=True)
        sys.stdout.flush()
        driver = webdriver.Chrome(service=service, options=chrome_options)
        print("[OK] ChromeDriver configurado com sucesso! Navegador aberto.", flush=True)
        sys.stdout.flush()
        return driver
    except Exception as e:
        print(f"[ERRO] Erro ao configurar o ChromeDriver: {e}")
        print("\nPor favor, verifique:")
        print("1. Google Chrome está instalado")
        print("2. Você tem conexão com a internet (para baixar o ChromeDriver)")
        print("3. Execute: pip install webdriver-manager")
        raise

def criar_conta_github():
    """Automatiza o processo de criação de conta no GitHub"""
    driver = None
    try:
        print("[INFO] Iniciando automacao de criacao de conta no GitHub...", flush=True)
        sys.stdout.flush()
        print(f"[INFO] Email: {EMAIL}", flush=True)
        print(f"[INFO] Username: {USERNAME}", flush=True)
        print("\n[ATENCAO] Voce precisara resolver o captcha manualmente!", flush=True)
        print("[INFO] Aguarde 5 segundos antes de iniciar...\n", flush=True)
        sys.stdout.flush()
        time.sleep(5)
        
        # Configurar driver
        driver = configurar_driver()
        
        # Navegar para a pagina de registro
        print("[INFO] Acessando pagina de registro do GitHub...", flush=True)
        sys.stdout.flush()
        driver.get("https://github.com/signup")
        print("[INFO] Pagina carregada!", flush=True)
        sys.stdout.flush()
        
        # Aguardar a pagina carregar
        wait = WebDriverWait(driver, 20)
        
        # Preencher email
        print("[INFO] Preenchendo email...")
        email_field = wait.until(
            EC.presence_of_element_located((By.ID, "email"))
        )
        email_field.clear()
        email_field.send_keys(EMAIL)
        time.sleep(1)
        
        # Clicar em "Continue" apos email
        print("[INFO] Clicando em 'Continue' apos email...", flush=True)
        sys.stdout.flush()
        # Tentar varios seletores possiveis
        continue_button = None
        seletores = [
            "button[data-continue-to='password-container']",
            "button[type='submit']",
            "//button[contains(text(), 'Continue')]",
            "button.btn-primary"
        ]
        
        for seletor in seletores:
            try:
                if seletor.startswith("//"):
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.XPATH, seletor))
                    )
                else:
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.CSS_SELECTOR, seletor))
                    )
                break
            except:
                continue
        
        if continue_button:
            continue_button.click()
            print("[OK] Botao Continue clicado!", flush=True)
        else:
            print("[AVISO] Nao foi possivel encontrar o botao Continue automaticamente.", flush=True)
            print("[INFO] Por favor, clique manualmente no botao Continue.", flush=True)
        sys.stdout.flush()
        time.sleep(3)
        
        # Preencher senha
        print("[INFO] Preenchendo senha...")
        password_field = wait.until(
            EC.presence_of_element_located((By.ID, "password"))
        )
        password_field.clear()
        password_field.send_keys(PASSWORD)
        time.sleep(1)
        
        # Clicar em "Continue" apos senha
        print("[INFO] Clicando em 'Continue' apos senha...", flush=True)
        sys.stdout.flush()
        continue_button = None
        seletores = [
            "button[data-continue-to='username-container']",
            "button[type='submit']",
            "//button[contains(text(), 'Continue')]",
            "button.btn-primary"
        ]
        
        for seletor in seletores:
            try:
                if seletor.startswith("//"):
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.XPATH, seletor))
                    )
                else:
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.CSS_SELECTOR, seletor))
                    )
                break
            except:
                continue
        
        if continue_button:
            continue_button.click()
            print("[OK] Botao Continue clicado!", flush=True)
        else:
            print("[AVISO] Nao foi possivel encontrar o botao Continue automaticamente.", flush=True)
            print("[INFO] Por favor, clique manualmente no botao Continue.", flush=True)
        sys.stdout.flush()
        time.sleep(3)
        
        # Preencher username
        print("[INFO] Preenchendo username...")
        username_field = wait.until(
            EC.presence_of_element_located((By.ID, "login"))
        )
        username_field.clear()
        username_field.send_keys(USERNAME)
        time.sleep(1)
        
        # Verificar se o username esta disponivel (aguardar validacao)
        print("[INFO] Aguardando validacao do username...")
        time.sleep(3)
        
        # Perguntar sobre emails de produto (geralmente aparece um checkbox)
        try:
            print("[INFO] Verificando opcao de emails de produto...")
            # Pode haver um checkbox para receber emails do GitHub
            # Vamos deixar desmarcado (padrão)
            time.sleep(1)
        except:
            pass
        
        # Clicar em "Continue" apos username
        print("[INFO] Clicando em 'Continue' apos username...", flush=True)
        sys.stdout.flush()
        continue_button = None
        seletores = [
            "button[data-continue-to='opt-in-container']",
            "button[type='submit']",
            "//button[contains(text(), 'Continue')]",
            "button.btn-primary"
        ]
        
        for seletor in seletores:
            try:
                if seletor.startswith("//"):
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.XPATH, seletor))
                    )
                else:
                    continue_button = wait.until(
                        EC.element_to_be_clickable((By.CSS_SELECTOR, seletor))
                    )
                break
            except:
                continue
        
        if continue_button:
            continue_button.click()
            print("[OK] Botao Continue clicado!", flush=True)
        else:
            print("[AVISO] Nao foi possivel encontrar o botao Continue automaticamente.", flush=True)
            print("[INFO] Por favor, clique manualmente no botao Continue.", flush=True)
        sys.stdout.flush()
        time.sleep(3)
        
        # Verificar opcao de verificacao (pode haver um captcha aqui)
        print("[INFO] Verificando se ha captcha...")
        time.sleep(2)
        
        print("\n" + "="*60)
        print("[OK] FORMULARIO PREENCHIDO COM SUCESSO!")
        print("="*60)
        print("\n[ATENCAO] ACAO MANUAL NECESSARIA:")
        print("1. Resolva o captcha se aparecer na tela")
        print("2. Complete o processo de verificacao")
        print("3. Verifique seu email: " + EMAIL)
        print("4. Clique no link de verificacao no email")
        print("\n[INFO] O navegador permanecera aberto para voce completar o processo.")
        print("="*60)
        
        # Manter o navegador aberto
        print("\n[INFO] Navegador permanecera aberto. Complete o processo manualmente.", flush=True)
        print("[INFO] Quando terminar, feche o navegador manualmente.", flush=True)
        try:
            input("\nPressione ENTER quando terminar de verificar a conta...")
        except EOFError:
            print("[INFO] Executando sem terminal interativo. Navegador permanecera aberto.", flush=True)
            time.sleep(60)  # Aguarda 60 segundos antes de fechar automaticamente
        
    except Exception as e:
        print(f"\n[ERRO] Erro durante a automacao: {e}", flush=True)
        print("\nO navegador permanecera aberto para voce verificar o que aconteceu.", flush=True)
        try:
            input("\nPressione ENTER para fechar...")
        except EOFError:
            print("[INFO] Executando sem terminal interativo. Navegador permanecera aberto por 60 segundos.", flush=True)
            time.sleep(60)
    finally:
        if driver:
            print("\n[INFO] Fechando navegador...")
            driver.quit()

if __name__ == "__main__":
    print("="*60)
    print("AUTOMACAO DE CRIACAO DE CONTA GITHUB")
    print("="*60)
    print("\nDados da conta:")
    print(f"   Email: {EMAIL}")
    print(f"   Username: {USERNAME}")
    print(f"   Senha: {'*' * len(PASSWORD)}")
    print("\nLembre-se:")
    print("   - Voce precisara resolver o captcha manualmente")
    print("   - Voce precisara verificar o email manualmente")
    print("   - O script apenas preenche o formulario")
    print("\n" + "="*60 + "\n")
    
    try:
        resposta = input("Deseja continuar? (s/n): ").lower().strip()
        if resposta in ['s', 'sim', 'y', 'yes']:
            criar_conta_github()
        else:
            print("Operacao cancelada.")
    except EOFError:
        # Se executado sem input disponivel, continua automaticamente
        print("[INFO] Executando automaticamente...")
        criar_conta_github()


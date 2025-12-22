"""Teste simples para verificar se o navegador abre"""
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import sys

print("="*60)
print("TESTE DE ABERTURA DO NAVEGADOR")
print("="*60)
print()

try:
    print("[1/4] Configurando ChromeDriver...", flush=True)
    chrome_options = Options()
    chrome_options.add_experimental_option("detach", True)
    chrome_options.add_argument("--start-maximized")
    
    print("[2/4] Instalando/baixando ChromeDriver...", flush=True)
    service = Service(ChromeDriverManager().install())
    
    print("[3/4] Abrindo navegador Chrome...", flush=True)
    driver = webdriver.Chrome(service=service, options=chrome_options)
    
    print("[4/4] Navegador aberto! Acessando GitHub...", flush=True)
    driver.get("https://github.com/signup")
    
    print()
    print("="*60)
    print("[OK] NAVEGADOR ABERTO COM SUCESSO!")
    print("="*60)
    print()
    print("O navegador deve estar visivel agora.")
    print("Aguardando 10 segundos para voce verificar...")
    print()
    
    time.sleep(10)
    
    print("Fechando navegador...")
    driver.quit()
    print("Teste concluido!")
    
except Exception as e:
    print()
    print("="*60)
    print("[ERRO] FALHA NO TESTE")
    print("="*60)
    print(f"Erro: {e}")
    print()
    import traceback
    traceback.print_exc()
    input("Pressione ENTER para sair...")


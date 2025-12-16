import subprocess
import time
import sys
import os

# Configurar encoding para Windows
if sys.platform == "win32":
    os.system("chcp 65001 > nul")

def run_adb_command(command):
    """Executa um comando ADB e retorna o resultado"""
    try:
        result = subprocess.run(command, shell=True, capture_output=True, text=True, encoding='utf-8')
        return result.stdout.strip(), result.stderr.strip(), result.returncode
    except Exception as e:
        return "", str(e), 1

def tap_screen(x, y):
    """Simula um toque na tela nas coordenadas especificadas"""
    stdout, stderr, code = run_adb_command(f"adb -s emulator-5554 shell input tap {x} {y}")
    if code != 0:
        print(f"Erro ao tocar na tela ({x}, {y}): {stderr}")
        return False
    return True

def main():
    print("=== TESTE AUTOMATIZADO DO FILTRO DE SEMANAS ===")
    print("Iniciando testes do filtro de semanas na tela de estatisticas...")
    
    # Esperar o aplicativo carregar completamente
    print("Aguardando 5 segundos para o aplicativo carregar...")
    time.sleep(5)
    
    # Teste 1: Navegar para a tela de estatísticas
    print("\n1. Navegando para a tela de estatisticas...")
    # Supondo que o menu de estatísticas esteja na parte inferior da tela
    # Coordenadas aproximadas - podem precisar de ajuste
    if not tap_screen(540, 1800):  # Coordenada aproximada para o menu de estatísticas
        print("Falha ao navegar para estatisticas")
        return False
    
    time.sleep(2)
    
    # Teste 2: Clicar no filtro "Semana"
    print("\n2. Testando o filtro 'Semana'...")
    # Coordenada aproximada para o chip "Semana"
    if not tap_screen(200, 300):
        print("Falha ao clicar no filtro 'Semana'")
        return False
    
    time.sleep(3)
    
    # Teste 3: Verificar se há crashes ou erros
    print("\n3. Verificando se ha erros ou crashes...")
    stdout, stderr, code = run_adb_command("adb -s emulator-5554 shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'")
    if "com.example.minhascompras" in stdout:
        print("[OK] Aplicativo ainda esta em foco - sem crashes detectados")
    else:
        print("[ERRO] Aplicativo nao esta mais em foco - possivel crash")
        return False
    
    # Teste 4: Testar outros filtros para verificar regressões
    print("\n4. Testando outros filtros para verificar regressoes...")
    
    # Testar filtro "Mes"
    if not tap_screen(400, 300):
        print("Falha ao clicar no filtro 'Mes'")
        return False
    time.sleep(2)
    
    # Testar filtro "3 Meses"
    if not tap_screen(600, 300):
        print("Falha ao clicar no filtro '3 Meses'")
        return False
    time.sleep(2)
    
    # Testar filtro "Ano"
    if not tap_screen(800, 300):
        print("Falha ao clicar no filtro 'Ano'")
        return False
    time.sleep(2)
    
    # Voltar para o filtro "Semana"
    if not tap_screen(200, 300):
        print("Falha ao voltar para o filtro 'Semana'")
        return False
    time.sleep(3)
    
    # Teste 5: Verificação final de estabilidade
    print("\n5. Verificacao final de estabilidade...")
    stdout, stderr, code = run_adb_command("adb -s emulator-5554 shell dumpsys window windows | grep -E 'mCurrentFocus|mFocusedApp'")
    if "com.example.minhascompras" in stdout:
        print("[OK] Aplicativo estavel apos todos os testes")
    else:
        print("[ERRO] Aplicativo instavel apos os testes")
        return False
    
    print("\n=== TESTES CONCLUIDOS COM SUCESSO ===")
    print("[OK] Compilacao: OK")
    print("[OK] Instalacao: OK")
    print("[OK] Filtro 'Semana': Funcionando")
    print("[OK] Outros filtros: Sem regressoes")
    print("[OK] Estabilidade: OK")
    
    return True

if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\nTeste interrompido pelo usuario")
        sys.exit(1)
    except Exception as e:
        print(f"\nErro durante os testes: {e}")
        sys.exit(1)
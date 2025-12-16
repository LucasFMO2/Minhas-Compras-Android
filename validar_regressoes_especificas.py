import subprocess
import time
import sys

def run_adb_command(command):
    """Executa um comando ADB e retorna o resultado"""
    try:
        result = subprocess.run(command, shell=True, capture_output=True, text=True)
        return result.stdout.strip(), result.stderr.strip(), result.returncode
    except Exception as e:
        return "", str(e), 1

def clear_logcat():
    """Limpa o logcat"""
    run_adb_command("adb -s emulator-5554 logcat -c")

def get_logcat_errors():
    """Verifica se há erros no logcat"""
    stdout, stderr, code = run_adb_command("adb -s emulator-5554 logcat -d | findstr -i 'exception error crash fatal'")
    return stdout

def test_date_utils_functionality():
    """Testa se as funções do DateUtils.kt estão funcionando corretamente"""
    print("Testando funcionalidades do DateUtils...")
    
    # Navega para a tela de estatísticas
    run_adb_command("adb -s emulator-5554 shell input tap 100 100")  # Abre drawer
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 300 600")  # Vai para Estatísticas
    time.sleep(2)
    
    # Testa o filtro de semanas (que usa DateUtils)
    run_adb_command("adb -s emulator-5554 shell input tap 900 1000")  # Filtro de semanas
    time.sleep(2)
    
    # Verifica se há erros relacionados ao DateUtils
    errors = get_logcat_errors()
    if "DateUtils" in errors and ("Exception" in errors or "Error" in errors):
        print(f"[ERRO] Erro encontrado no DateUtils: {errors}")
        return False
    else:
        print("[OK] DateUtils funcionando corretamente")
        return True

def test_statistics_screen():
    """Testa a tela de estatísticas em detalhes"""
    print("\nTestando tela de estatísticas...")
    
    # Testa diferentes filtros
    filters = [
        (300, 1000, "Mês"),
        (500, 1000, "3 Meses"),
        (700, 1000, "Ano"),
        (900, 1000, "Semanas")
    ]
    
    for x, y, name in filters:
        run_adb_command(f"adb -s emulator-5554 shell input tap {x} {y}")
        time.sleep(2)
        
        # Verifica se há erros após mudar o filtro
        errors = get_logcat_errors()
        if "Exception" in errors or "Error" in errors or "FATAL" in errors:
            print(f"[ERRO] Erro ao usar filtro {name}: {errors}")
            return False
        else:
            print(f"[OK] Filtro {name} funcionando corretamente")
    
    return True

def test_period_filter_chips():
    """Testa especificamente o PeriodFilterChips.kt"""
    print("\nTestando PeriodFilterChips...")
    
    # Navega para estatísticas se não estiver lá
    run_adb_command("adb -s emulator-5554 shell input keyevent KEYCODE_BACK")  # Volta
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 100 100")  # Abre drawer
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 300 600")  # Vai para Estatísticas
    time.sleep(2)
    
    # Testa clicar em diferentes posições dos chips
    chip_positions = [
        (300, 1000),  # Primeiro chip
        (500, 1000),  # Segundo chip
        (700, 1000),  # Terceiro chip
        (900, 1000)   # Quarto chip (semanas)
    ]
    
    for x, y in chip_positions:
        run_adb_command(f"adb -s emulator-5554 shell input tap {x} {y}")
        time.sleep(1.5)
        
        # Verifica se há erros relacionados ao PeriodFilterChips
        errors = get_logcat_errors()
        if "PeriodFilterChips" in errors and ("Exception" in errors or "Error" in errors):
            print(f"[ERRO] Erro no PeriodFilterChips: {errors}")
            return False
    
    print("[OK] PeriodFilterChips funcionando corretamente")
    return True

def test_statistics_viewmodel():
    """Testa o StatisticsViewModel.kt"""
    print("\nTestando StatisticsViewModel...")
    
    # Testa mudar rapidamente entre filtros para testar o ViewModel
    for i in range(3):
        run_adb_command("adb -s emulator-5554 shell input tap 300 1000")  # Mês
        time.sleep(0.5)
        run_adb_command("adb -s emulator-5554 shell input tap 500 1000")  # 3 Meses
        time.sleep(0.5)
        run_adb_command("adb -s emulator-5554 shell input tap 700 1000")  # Ano
        time.sleep(0.5)
        run_adb_command("adb -s emulator-5554 shell input tap 900 1000")  # Semanas
        time.sleep(0.5)
    
    # Verifica se há erros no ViewModel
    errors = get_logcat_errors()
    if "StatisticsViewModel" in errors and ("Exception" in errors or "Error" in errors):
        print(f"[ERRO] Erro no StatisticsViewModel: {errors}")
        return False
    else:
        print("[OK] StatisticsViewModel funcionando corretamente")
        return True

def test_shopping_list_functionality():
    """Testa se a lista de compras ainda funciona corretamente"""
    print("\nTestando funcionalidades da lista de compras...")
    
    # Volta para a tela principal
    run_adb_command("adb -s emulator-5554 shell input keyevent KEYCODE_BACK")
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 100 100")  # Abre drawer
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 300 400")  # Vai para Lista
    time.sleep(2)
    
    # Testa adicionar um item
    run_adb_command("adb -s emulator-5554 shell input tap 900 1800")  # Botão adicionar
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input text 'Teste Regressão'")  # Digita texto
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 1000 1200")  # Salva
    time.sleep(2)
    
    # Verifica se há erros
    errors = get_logcat_errors()
    if "Exception" in errors or "Error" in errors or "FATAL" in errors:
        print(f"[ERRO] Erro na lista de compras: {errors}")
        return False
    else:
        print("[OK] Lista de compras funcionando corretamente")
        return True

def test_history_functionality():
    """Testa se a tela de histórico funciona corretamente"""
    print("\nTestando tela de histórico...")
    
    # Navega para o histórico
    run_adb_command("adb -s emulator-5554 shell input tap 100 100")  # Abre drawer
    time.sleep(1)
    run_adb_command("adb -s emulator-5554 shell input tap 300 800")  # Vai para Histórico
    time.sleep(2)
    
    # Verifica se há erros
    errors = get_logcat_errors()
    if "Exception" in errors or "Error" in errors or "FATAL" in errors:
        print(f"[ERRO] Erro na tela de histórico: {errors}")
        return False
    else:
        print("[OK] Tela de histórico funcionando corretamente")
        return True

def main():
    print("=== VALIDAÇÃO ESPECÍFICA DE REGRESSÕES ===\n")
    
    # Limpa o logcat antes de começar
    clear_logcat()
    
    tests = [
        ("Funcionalidades do DateUtils", test_date_utils_functionality),
        ("Tela de Estatísticas", test_statistics_screen),
        ("PeriodFilterChips", test_period_filter_chips),
        ("StatisticsViewModel", test_statistics_viewmodel),
        ("Lista de Compras", test_shopping_list_functionality),
        ("Tela de Histórico", test_history_functionality)
    ]
    
    passed = 0
    failed = 0
    
    for test_name, test_func in tests:
        print(f"\n--- {test_name} ---")
        clear_logcat()  # Limpa o logcat antes de cada teste
        
        try:
            if test_func():
                passed += 1
                print(f"[OK] {test_name}: PASSOU")
            else:
                failed += 1
                print(f"[ERRO] {test_name}: FALHOU")
        except Exception as e:
            failed += 1
            print(f"[ERRO] {test_name}: ERRO - {str(e)}")
    
    print("\n=== RESUMO DA VALIDAÇÃO ===")
    print(f"[OK] Testes passados: {passed}")
    print(f"[ERRO] Testes falhados: {failed}")
    print(f"[INFO] Total de testes: {passed + failed}")
    
    if failed == 0:
        print("\n[SUCESSO] TODOS OS TESTES PASSARAM! Não há regressões específicas detectadas.")
        print("As correções no filtro de semanas não afetaram outras funcionalidades.")
        return True
    else:
        print(f"\n[ALERTA] {failed} teste(s) falharam. É necessário investigar as regressões específicas.")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
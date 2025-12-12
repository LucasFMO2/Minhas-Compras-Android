import os
import re

def analyze_file(filepath, patterns):
    """Analisa um arquivo em busca de padrões específicos"""
    try:
        with open(filepath, 'r', encoding='utf-8') as file:
            content = file.read()
            results = {}
            for pattern_name, pattern in patterns.items():
                matches = re.findall(pattern, content, re.MULTILINE | re.DOTALL)
                results[pattern_name] = {
                    'found': len(matches) > 0,
                    'count': len(matches),
                    'matches': matches[:3]  # Primeiras 3 correspondências
                }
            return results
    except Exception as e:
        return {'error': str(e)}

def main():
    print("=== VALIDAÇÃO DO FILTRO DE SEMANAS ===")
    print("Analisando implementações do filtro de semanas...\n")
    
    # Análise do DateUtils.kt
    print("1. Analisando DateUtils.kt...")
    dateutils_patterns = {
        'getStartOfWeek': r'fun\s+getStartOfWeek\s*\([^)]*\)\s*:\s*Long',
        'getPreviousWeekStart': r'fun\s+getPreviousWeekStart\s*\([^)]*\)\s*:\s*Long',
        'isValidPeriod': r'fun\s+isValidPeriod\s*\([^)]*\)\s*:\s*Boolean',
        'calendar_monday_alignment': r'Calendar\.MONDAY',
        'week_start_calculation': r'add\(Calendar\.DAY_OF_YEAR'
    }
    
    dateutils_results = analyze_file('app/src/main/java/com/example/minhascompras/ui/utils/DateUtils.kt', dateutils_patterns)
    
    for pattern, result in dateutils_results.items():
        if 'error' in result:
            print(f"  [ERRO] {pattern}: {result['error']}")
        elif result['found']:
            print(f"  [OK] {pattern}: Implementado ({result['count']} ocorrência(s))")
        else:
            print(f"  [FALTA] {pattern}: Não encontrado")
    
    # Análise do PeriodFilterChips.kt
    print("\n2. Analisando PeriodFilterChips.kt...")
    filterchips_patterns = {
        'week_chip': r'FilterChip\s*\(\s*selected\s*=\s*selectedPeriod\.type\s*==\s*PeriodType\.WEEK',
        'dateutils_import': r'import.*DateUtils',
        'week_chip_onclick': r'onClick\s*=\s*\{[^}]*DateUtils\.getStartOfWeek[^}]*\}',
        'period_creation': r'Period\s*\(\s*type\s*=\s*PeriodType\.WEEK'
    }
    
    filterchips_results = analyze_file('app/src/main/java/com/example/minhascompras/ui/components/PeriodFilterChips.kt', filterchips_patterns)
    
    for pattern, result in filterchips_results.items():
        if 'error' in result:
            print(f"  [ERRO] {pattern}: {result['error']}")
        elif result['found']:
            print(f"  [OK] {pattern}: Implementado ({result['count']} ocorrência(s))")
        else:
            print(f"  [FALTA] {pattern}: Não encontrado")
    
    # Análise do StatisticsViewModel.kt
    print("\n3. Analisando StatisticsViewModel.kt...")
    viewmodel_patterns = {
        'dateutils_import': r'import.*DateUtils',
        'period_validation': r'DateUtils\.isValidPeriod',
        'week_period_type': r'PeriodType\.WEEK',
        'error_handling': r'catch\s*\([^)]*Exception[^)]*\)',
        'cache_implementation': r'private.*Cache\s*=\s*mutableMapOf'
    }
    
    viewmodel_results = analyze_file('app/src/main/java/com/example/minhascompras/ui/viewmodel/StatisticsViewModel.kt', viewmodel_patterns)
    
    for pattern, result in viewmodel_results.items():
        if 'error' in result:
            print(f"  [ERRO] {pattern}: {result['error']}")
        elif result['found']:
            print(f"  [OK] {pattern}: Implementado ({result['count']} ocorrência(s))")
        else:
            print(f"  [FALTA] {pattern}: Não encontrado")
    
    # Análise do StatisticsScreen.kt
    print("\n4. Analisando StatisticsScreen.kt...")
    screen_patterns = {
        'dateutils_import': r'import.*DateUtils',
        'previous_period_calculation': r'getPreviousWeekStart',
        'week_period_handling': r'PeriodType\.WEEK\s*->',
        'period_validation': r'validPreviousPeriod\s*=\s*remember',
        'error_handling': r'catch\s*\([^)]*Exception[^)]*\)'
    }
    
    screen_results = analyze_file('app/src/main/java/com/example/minhascompras/ui/screens/StatisticsScreen.kt', screen_patterns)
    
    for pattern, result in screen_results.items():
        if 'error' in result:
            print(f"  [ERRO] {pattern}: {result['error']}")
        elif result['found']:
            print(f"  [OK] {pattern}: Implementado ({result['count']} ocorrência(s))")
        else:
            print(f"  [FALTA] {pattern}: Não encontrado")
    
    # Verificação de consistência
    print("\n5. Verificação de consistência entre arquivos...")
    
    # Verificar se DateUtils é importado em todos os arquivos necessários
    dateutils_imports = [
        filterchips_results.get('dateutils_import', {}).get('found', False),
        viewmodel_results.get('dateutils_import', {}).get('found', False),
        screen_results.get('dateutils_import', {}).get('found', False)
    ]
    
    if all(dateutils_imports):
        print("  [OK] DateUtils importado em todos os arquivos necessários")
    else:
        print("  [AVISO] DateUtils não importado em todos os arquivos necessários")
    
    # Verificar se há tratamento de erros
    error_handling = [
        viewmodel_results.get('error_handling', {}).get('found', False),
        screen_results.get('error_handling', {}).get('found', False)
    ]
    
    if all(error_handling):
        print("  [OK] Tratamento de erros implementado")
    else:
        print("  [AVISO] Tratamento de erros pode estar faltando")
    
    # Resumo
    print("\n=== RESUMO DA VALIDAÇÃO ===")
    
    all_checks = []
    
    # Verificar implementações críticas
    critical_checks = [
        dateutils_results.get('getStartOfWeek', {}).get('found', False),
        dateutils_results.get('getPreviousWeekStart', {}).get('found', False),
        dateutils_results.get('isValidPeriod', {}).get('found', False),
        filterchips_results.get('week_chip', {}).get('found', False),
        filterchips_results.get('week_chip_onclick', {}).get('found', False),
        viewmodel_results.get('period_validation', {}).get('found', False),
        screen_results.get('previous_period_calculation', {}).get('found', False)
    ]
    
    if all(critical_checks):
        print("[SUCESSO] Todas as implementações críticas do filtro de semanas estão presentes")
        print("[SUCESSO] O filtro de semanas deve funcionar corretamente")
        return True
    else:
        print("[FALHA] Algumas implementações críticas estão faltando")
        missing = []
        if not dateutils_results.get('getStartOfWeek', {}).get('found', False):
            missing.append("getStartOfWeek")
        if not dateutils_results.get('getPreviousWeekStart', {}).get('found', False):
            missing.append("getPreviousWeekStart")
        if not dateutils_results.get('isValidPeriod', {}).get('found', False):
            missing.append("isValidPeriod")
        if not filterchips_results.get('week_chip', {}).get('found', False):
            missing.append("week_chip")
        if not filterchips_results.get('week_chip_onclick', {}).get('found', False):
            missing.append("week_chip_onclick")
        if not viewmodel_results.get('period_validation', {}).get('found', False):
            missing.append("period_validation")
        if not screen_results.get('previous_period_calculation', {}).get('found', False):
            missing.append("previous_period_calculation")
        
        print(f"[FALHA] Implementações faltando: {', '.join(missing)}")
        return False

if __name__ == "__main__":
    try:
        success = main()
        exit(0 if success else 1)
    except Exception as e:
        print(f"Erro durante a validação: {e}")
        exit(1)
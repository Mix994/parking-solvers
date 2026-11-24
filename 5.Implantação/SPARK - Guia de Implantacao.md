# Guia de Implantação — SmartPark

## 1. Pré-requisitos
- **Android Studio** (versão com suporte a Gradle 9.6 / AGP 9.5 / JDK 11+).
- **Android SDK** com `compileSdk = 37` e dispositivo ou emulador com **Android 7.0 (API 24) ou superior**.

## 2. Abrir o Projeto no Android Studio
1. Clone o repositório completo.
2. No Android Studio, selecione **File > Open** e aponte para a pasta `3.Implementacao/`.
3. Aguarde a sincronização do Gradle (download das dependências Compose, Navigation e Room + KSP).

## 3. Executar Testes e Gerar o APK Instalável
No terminal, a partir da pasta `3.Implementacao/`:

```powershell
# Executar todos os testes unitários
.\gradlew.bat testDebugUnitTest

# Gerar o APK instalável de debug
.\gradlew.bat assembleDebug
```

O APK será gerado em:
`3.Implementacao/app/build/outputs/apk/debug/app-debug.apk`

## 4. Instalação no Dispositivo / Emulador
- Pelo Android Studio: clique em **Run (`Shift + F10`)**.
- Via ADB:
  ```powershell
  adb install -r "3.Implementacao\app\build\outputs\apk\debug\app-debug.apk"
  ```
- Na primeira abertura, o banco Room (`parking_solvers_smartpark.db`, versão 3) é criado e preenchido automaticamente com dados fictícios de demonstração (`DadosIniciais`).

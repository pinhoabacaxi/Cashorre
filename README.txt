Pinhoabacaxi Músicas (offline mp3/m4a player) - Projeto Android Studio (Kotlin + Jetpack Compose + Media3)

O que faz:
- Tela "Músicas": lista playlists (pastas em filesDir/midias/)
- Tela "Procurar músicas": abre o app Files (SAF) para selecionar uma pasta e importar .mp3/.m4a
- Copia os arquivos para a pasta interna do app: /midias/<playlist>/
- Ordem das músicas é salva em /midias/<playlist>/order.json
- Player com play/pause, próxima/anterior e barra de progresso
- Controle de volume "do app" via volume interno do player (slider)

Importante (limitações reais do Android):
- O Android não permite controlar o volume "só no app" pela barra do sistema de forma 100% independente.
  Aqui isso é feito com um multiplicador interno (player.volume).
- Para acessar arquivos do armazenamento sem permissões invasivas, usamos Storage Access Framework (OpenDocumentTree).
  Isso evita pedir "MANAGE_EXTERNAL_STORAGE".
- "Gerenciar chamadas" não é necessário para um player de música e não é recomendado.

Como gerar APK:
1) Abra a pasta do projeto no Android Studio
2) Build > Build Bundle(s)/APK(s) > Build APK(s)
3) APK em: app/build/outputs/apk/debug/app-debug.apk

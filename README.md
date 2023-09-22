# FlexSign

Простий SpringBoot застосунок, який наразі підтримує лише 1 функцію:

- Підписання документів за допомогою підпису згенерованого через ПриватБанк (.jks)

# Як протестувати

Це web застосунок який після запуску доступний за адресою: `http://localhost:8080`

Для того щоб підписати документ, потрібно надіслати HTTP POST запит за наступною адресою:
`http:localhost:8080/sign`, тіло запиту має бути надіслано як form-data та містити наступні ключі:

- fileToBeSigned (файл на підпис)
- signatureFile (файл самого підпису .jks)
- password (пароль до файлу підпису - текст)

У відповідь прийде підписаний документ.

#### Приклад з CURL
```shell
curl \
  -F "fileToBeSigned=@/path/to/document/contract-123.pdf" \
  -F "signatureFile=@/path/to/signature/12345678_12345678.jks" \
  -F "password=MySecurePassword" \
  http://localhost:8080/sign \
  -o ./signed-contract-123.pdf
```

# Як запустити

### За допомогою Docker

```shell
docker build -t flex-sign:latest . && docker run -p 8080:8080 -it flex-sign:latest
```

### За допомогою DockerCompose

```shell
docker compose up --build
```

### Як java застосунок

#### Вимоги перед запуском

- Встановлена java17
- OS: Ubuntu чи MacOS (не М1)

#### Запуск

```shell
# Увійти в директорію проекту
cd ./FlexSign
# Зібрати jar файл
./gradlew clean bootJar
# Запустити
java -jar ./build/libs/FlexSign-1.0.0.jar
```

# Дисклеймер

Проект розроблено з навчальною метою, тож може містити баги - використання на свій розсуд.

В основі лежить сертифікована бібліотека uapki - https://github.com/specinfo-ua/UAPKI, 

висновок державної експертизи - https://data.gov.ua/dataset/7b0d45fe-75eb-4d14-9792-59e440305678

Посилання на Телеграм канал для розробників - https://t.me/joinchat/UTjOABGHYxEqUYDp

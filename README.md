# searchengine 
## Описание
Поисковый движок по сайту, написанный на языке Java, использующий фреймворк Spring.
Позволяет индексировать страницы и осуществлять по ним быстрый поиск.

## Сборка проекта
Для сборки понадобится MySQL 8.*, установленный локально либо в сети. 
Вам необходимо отредактировать конфигурационный файл application.yaml, заменив путь к базе данных, а также указав ваш логин и пароль к серверу.

#### application.yaml:
```yaml
spring:
  datasource:
    username: YOUR_LOGIN
    password: YOUR_PASSWORD
    url: jdbc:mysql://localhost:3306/search_engine?useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true
```

**В проекте используются сгенерированные JAR библиотеки леммитизации из Maven репозитория https://gitlab.skillbox.ru/** .

Для их использования необходимо указать токен для доступа к данному Maven-репозиторию. Для указания токена найдите или создайте файл settings.xml.

- **В Windows** В Windows он располагается в директории
  - C:/Users/<Имя вашего пользователя>/.m2
- **В Linux** — в директории
  - /home/<Имя вашего пользователя>/.m2
- **В macOs** — по адресу
  - /Users/<Имя вашего пользователя>/.m2

Если файла settings.xml нет, создайте его и вставьте в него код:

#### settings.xml:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
https://maven.apache.org/xsd/settings-1.0.0.xsd">
   <servers>
      <server>
         <id>skillbox-gitlab</id>
         <configuration>
            <httpHeaders>
               <property>
                  <name>Private-Token</name>
                  <value>wtb5axJDFX9Vm_W1Lexg</value>
               </property>
            </httpHeaders>
         </configuration>
      </server>
   </servers>
</settings>
```

В блоке <value> находится уникальный токен доступа. Если у вас возникнет «401 Ошибка Авторизации» при попытке получения зависимостей, 
возьмите актуальный токен доступа из документа по [ссылке](https://docs.google.com/document/d/1rb0ysFBLQltgLTvmh-ebaZfJSI7VwlFlEYT9V5_aPjc/edit).

## Как осуществляется поиск
После запуска проекта, по адресу http://localhost:8080/ станет доступен веб-интерфейс. Он представляет собой одну веб-страницу с тремя вкладками:

### Dashboard. 
Эта вкладка открывается по умолчанию. На ней отображается общая статистика по всем сайтам, а также детальная статистика и статус по каждому из сайтов.

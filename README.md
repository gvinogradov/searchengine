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
<p align="center">
<img src="https://user-images.githubusercontent.com/8067668/217037151-67e69b1f-7cfc-4d03-a845-a8c474d59516.png" width="100%"></p>

### Management.
На этой вкладке находятся инструменты управления поисковым движком — запуск и остановка полной индексации (переиндексации), а также возможность добавить (обновить) отдельную страницу по ссылке:
<p align="center">
<img src="https://user-images.githubusercontent.com/8067668/217037157-904d6e0f-b6a1-4b2d-b850-f436c426aac2.png" width="100%"></p>
  
### Search.
Эта страница предназначена для тестирования поискового движка. На ней находится поле поиска, выпадающий список с выбором сайта для поиска, а при нажатии на кнопку «Найти» выводятся результаты поиска:
<p align="center">
<img src="https://user-images.githubusercontent.com/8067668/217037160-7d75c9ce-f85a-4c64-81a7-3af123033006.png" width="100%"></p>

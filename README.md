# Лаборатори №8: Mockito mock объектууд

**Оюутан:** Delgermaa

**Код:** b232270035

## Орчны хувилбар

`java -version`:

```text
openjdk version "21.0.13" 2026-10-20
OpenJDK Runtime Environment (build 21.0.13+9-deb13u2)
OpenJDK 64-Bit Server VM (build 21.0.13+9-deb13u2, mixed mode, sharing)
```

`mvn -version`:

```text
Apache Maven 3.9.9
Maven home: /usr/share/maven
Java version: 21.0.13, vendor: Debian, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.12.117+deb13-amd64", arch: "amd64", family: "unix"
```

## Тестийн үр дүн

`mvn test`-ийн `results/mvn-test.txt` гаралт: **10 тест, 0 алдаа, 0 failure** (`BUILD SUCCESS`). Java 21 дээр Mockito inline mock maker-ийн dynamic Java agent тухай анхааруулга гарсан; тестийн үр дүнд нөлөөлөхгүй.

A8-д CSV-ийн мөр бүр өөр салбар руу эрт буцдаг тул тухайн мөрөнд ашиглагдахгүй зарим stub үлддэг. Mockito extension-ийн strict stubs горим үүнийг `UnnecessaryStubbingException` гэж үзэхээс сэргийлж, зөвхөн A8 доторх салбарын stub-уудыг `lenient()` болгосон.

## Хэсэг B: Мутацийн шалгалт

| Мутант | Зориуд хийсэн өөрчлөлт | Унасан тестүүд | Гаралт |
|---|---|---|---|
| 1 | Идэвхгүй оюутны нөхцөлийг авсан | `inactiveStudent_returnsInactiveAndDoesNotInteractWithEmail`; `invalidRegistrationState_returnsExpectedErrorWithoutSaving` (inactive мөр) | 10 тест, 2 failure; `BUILD FAILURE` |
| 2 | `saveRegistration(...)` дуудалтыг авсан | `activeStudentWithCompletedPrerequisites_registersAndSendsConfirmation`; `emailFailure_keepsRegistrationSuccessful` | 10 тест, 2 failure; `BUILD FAILURE` |
| 3 | Эхний null шалгалтаас `courseId == null` хэсгийг авсан | `nullArguments_throwIllegalArgumentExceptionWithoutDependencyInteractions` | 10 тест, 1 failure; `BUILD FAILURE` |

Мутацийн дараа эх кодыг starter-ийн хэвийн хувилбарт сэргээж, ногоон тестийг `results/mvn-test.txt` файлд дахин хадгалсан. Мутац бүрийн бүрэн гаралт `results/mvn-test-mutant-1.txt`–`results/mvn-test-mutant-3.txt` файлуудад байна.

## Дүгнэлт

Mockito mock нь тестийн хүрээнд дуудлагын хариуг программчилж, харилцан үйлдлийг шалгах боломж олгодог. Fake нь интерфейсийг бодитоор хэрэгжүүлсэн энгийн объект бөгөөд өөрийн төлөв, логиктой байдаг. Database, SMTP зэрэг хилийн гаднах системийг нэгжийн тестэд mock хийх нь үйлчилгээний логикийг тусгаарлаж, тестийг хурдан бөгөөд давтагдахуйц болгодог. Харин тестлэж буй үйлчилгээний өөрийнх нь дотоод логик болон энгийн өгөгдлийн объектуудыг шаардлагагүйгээр mock болгож болохгүй. Бүх дуудлагад хэт `verify` тавибал хэрэгжүүлэлтийн жижиг өөрчлөлтөд тест унаж, тест brittle болно. Тиймээс гаднаас ажиглагдах чухал үр дүн, хадгалалт болон мэдэгдлийн контракт дээр л verify хийв. Мутацийн шалгалтууд гурван зориудын алдааг илрүүлсэн нь гол сөрөг замууд тестээр хамгаалагдсаныг харууллаа.

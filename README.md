# WireMock Training - Jalin

Proyek pelatihan untuk memahami penggunaan **WireMock** sebagai HTTP mock server dalam pengujian unit berbasis Java. Proyek ini mencakup contoh-contoh praktis mulai dari pembuatan stub secara programatik hingga konfigurasi mapping via file JSON, serta unit testing dasar dengan JUnit 4.

---

## Tujuan

- Memahami konsep API mocking menggunakan WireMock
- Mempraktikkan pembuatan HTTP stub untuk berbagai skenario: GET, POST, PUT, DELETE
- Memahami URL matching: exact, pattern (regex), dan path template
- Mempraktikkan response customization: body, header, status code
- Memahami prioritas stub dan fallback behavior
- Mempelajari konfigurasi stub via file JSON (mapping files)
- Memahami lifecycle testing JUnit 4 (`@BeforeClass`, `@Before`, `@After`, `@AfterClass`)

---

## Teknologi & Dependensi

| Library       | Versi   | Fungsi                                          |
|---------------|---------|-------------------------------------------------|
| Java          | 18      | Bahasa pemrograman utama                        |
| Maven         | -       | Build tool dan dependency management            |
| JUnit         | 4.13.2  | Framework unit testing                          |
| WireMock      | 3.9.1   | HTTP mock server untuk simulasi API             |
| OkHttp3       | 4.2.2   | HTTP client untuk mengirim request dalam test   |
| Lombok        | 1.18.34 | Mengurangi boilerplate code (getter, setter, builder) |

---

## Struktur Proyek

```
jalin-wiremock-training/
├── pom.xml
└── src/
    ├── main/java/com/braja/
    │   ├── Main.java                        # Entry point aplikasi
    │   ├── model/
    │   │   └── Employee.java                # Model data Employee (Lombok)
    │   └── service/
    │       ├── Calculator.java              # Service kalkulasi
    │       └── EmployeeService.java         # Service CRUD Employee (in-memory)
    └── test/
        ├── java/com/braja/
        │   ├── MainTest.java                # Test WireMock (stub programatik)
        │   └── service/
        │       ├── CalculatorTest.java      # Unit test Calculator
        │       └── EmployeeServiceTest.java # Unit test EmployeeService
        └── resources/
            ├── __files/
            │   └── output.json             # Body file untuk response WireMock
            └── mappings/
                ├── sampleMapping.json       # Contoh stub via mapping file
                └── otherMapping.json        # Contoh stub via mapping file
```

---

## Prasyarat

- **Java 18+** sudah terinstall
- **Maven 3.6+** sudah terinstall
- IDE seperti IntelliJ IDEA (disarankan)

Cek instalasi:
```bash
java -version
mvn -version
```

---

## Cara Install & Menjalankan

**1. Clone repositori**
```bash
git clone <repository-url>
cd jalin-wiremock-training
```

**2. Install dependensi**
```bash
mvn clean install
```

**3. Jalankan semua test**
```bash
mvn test
```

**4. Jalankan test tertentu**
```bash
# Jalankan hanya MainTest (WireMock test)
mvn test -Dtest=MainTest

# Jalankan hanya CalculatorTest
mvn test -Dtest=CalculatorTest

# Jalankan hanya EmployeeServiceTest
mvn test -Dtest=EmployeeServiceTest
```

---

## Fitur & Contoh

### 1. WireMock Stub Programatik (`MainTest.java`)

WireMock dijalankan otomatis via `@Rule` pada port **8080** selama test berlangsung.

| Test Method              | HTTP Method | URL                    | Skenario                                        |
|--------------------------|-------------|------------------------|-------------------------------------------------|
| `exampleTestBodyFile`    | GET         | `/test/bodyFile`       | Response body dari file `output.json`           |
| `exampleTest401`         | GET         | `/test/abcde`          | URL pattern matching → fallback status 401      |
| `exampleTestAbc`         | GET         | `/test/abc`            | Response dengan multi-value header              |
| `exampleTestDelete`      | DELETE      | `/test/delete`         | HTTP DELETE dengan response JSON                |
| `exampleTestPost`        | POST        | `/test/post`           | Request body matching (JSON equality)           |
| `exampleTestUnauthorized`| POST        | `/test/unauthorized`   | Simulasi response 401 Unauthorized              |
| `exampleTestFileMapping` | GET         | `/mappings/other`      | Stub dari file JSON mapping                     |

**Contoh stub programatik:**
```java
// Stub dengan response dari file
stubFor(get(urlEqualTo("/test/bodyFile"))
    .willReturn(aResponse().withBodyFile("output.json")));

// Stub dengan URL pattern matching (regex) dan prioritas fallback
stubFor(get(urlMatching("/test/.*")).atPriority(10)
    .willReturn(aResponse().withStatus(401)));

// Stub dengan custom response header
stubFor(get(urlEqualTo("/test/abc"))
    .willReturn(aResponse()
        .withHeader("x-trace-id", "abc")
        .withHeader("x-token", "def")
        .withBody("Test success!")));

// Stub dengan request body matching
stubFor(post("/test/post")
    .withRequestBody(equalToJson("{\"name\":\"Hendro\"}"))
    .willReturn(okJson("Posted!")));

// Stub status code khusus (I'm a teapot)
stubFor(put("/test/status-only").willReturn(status(418)));
```

---

### 2. WireMock Stub via JSON Mapping File

File JSON di `src/test/resources/mappings/` otomatis dimuat oleh WireMock.

**`sampleMapping.json`** — GET `/mappings/file` → response body teks:
```json
{
  "request": {
    "urlPathTemplate": "/mappings/file",
    "method": "GET"
  },
  "response": {
    "status": 200,
    "body": "Sample Mapping File"
  }
}
```

**`otherMapping.json`** — GET `/mappings/other` → response body teks:
```json
{
  "request": {
    "urlPathTemplate": "/mappings/other",
    "method": "GET"
  },
  "response": {
    "status": 200,
    "body": "Sample Mapping Other File"
  }
}
```

**Body file** di `src/test/resources/__files/output.json`:
```json
{
  "id": 1,
  "name": "braja"
}
```

---

### 3. Unit Test Calculator (`CalculatorTest.java`)

Mendemonstrasikan JUnit 4 lifecycle hooks dan assertion dasar.

```java
@Test
public void multiplyTest() {
    Assert.assertEquals(100, Calculator.multiply(10, 10));
}
```

Lifecycle order: `@BeforeClass` → `@Before` → `@Test` → `@After` → `@AfterClass`

---

### 4. Unit Test EmployeeService (`EmployeeServiceTest.java`)

Menguji operasi CRUD in-memory pada `EmployeeService`.

| Test Method   | Fungsi yang Diuji                               |
|---------------|-------------------------------------------------|
| `testFindAll` | Menyimpan 2 data dan memverifikasi seluruh list |
| `testFindOne` | Mencari employee berdasarkan ID                 |
| `testSave`    | Menyimpan 1 data dan memverifikasi tersimpan    |
| `testDelete`  | Menghapus data berdasarkan ID                   |

---

## URL Matching Modes di WireMock

| Method             | Deskripsi                                          | Contoh                          |
|--------------------|----------------------------------------------------|---------------------------------|
| `urlEqualTo`       | Exact match pada full URL path + query string      | `/test/abc`                     |
| `urlMatching`      | Regex match pada full URL path + query string      | `/test/.*`                      |
| `urlPathEqualTo`   | Exact match hanya pada path (ignore query string)  | `/test/abc`                     |
| `urlPathMatching`  | Regex match hanya pada path                        | `/test/[a-z]+`                  |
| `urlPathTemplate`  | Path template dengan variable                      | `/users/{id}`                   |

---

## Konsep Penting: Prioritas Stub

Ketika beberapa stub cocok dengan satu request, WireMock menggunakan **prioritas** untuk menentukan stub mana yang digunakan. Nilai prioritas lebih kecil = lebih diprioritaskan (default: 5).

```java
// Stub spesifik — tidak ada prioritas eksplisit, default lebih tinggi
stubFor(get(urlEqualTo("/test/abc"))
    .willReturn(aResponse().withBody("Test success!")));

// Stub fallback — prioritas 10 (lebih rendah), hanya dipanggil jika tidak ada yang cocok
stubFor(get(urlMatching("/test/.*")).atPriority(10)
    .willReturn(aResponse().withStatus(401)));
```

---

## Referensi

- [WireMock Documentation](https://wiremock.org/docs/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [OkHttp3 Documentation](https://square.github.io/okhttp/)
- [Lombok Documentation](https://projectlombok.org/)

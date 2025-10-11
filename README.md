# FRC jsifenlib (Fork de RSHK jsifenlib)

Fork de `rshk-jsifenlib` con correcciones para problemas de XML mal formado en eventos de cancelación.

[![GitHub Release](https://img.shields.io/github/v/release/GabFrank/rshk-jsifenlib)](https://github.com/GabFrank/rshk-jsifenlib/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🚀 ¿Por qué este fork?

La versión 0.2.4 publicada en Maven Central del proyecto original tiene un problema donde `Sifen.recepcionEvento(eventosDE)` genera XML mal formado para operaciones de cancelación. Este fork incluye:

- ✅ **Fix del XML mal formado** en eventos de cancelación
- ✅ **Dependencias SOAP incluidas** (saaj-impl:1.5.3, javax.xml.soap-api:1.4.0)
- ✅ **Sincronización con upstream** - Se mantiene actualizado con el proyecto original
- ✅ **Publicado en GitHub Packages** - Fácil acceso y distribución

## 📦 Instalación

### Requisitos previos

1. **Crear un Personal Access Token de GitHub**:
   - Ve a https://github.com/settings/tokens
   - Click en **"Generate new token (classic)"**
   - Selecciona el scope **`read:packages`**
   - Copia el token generado

### Maven (Spring Boot / Java)

**1. Configurar autenticación**

Edita o crea `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

**2. Agregar en tu `pom.xml`**:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/GabFrank/rshk-jsifenlib</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.gabfrank</groupId>
        <artifactId>jsifenlib</artifactId>
        <version>0.2.4-frc.13</version>
    </dependency>
</dependencies>
```

**3. Instalar**:

```bash
mvn clean install
```

### Gradle

**1. Configurar autenticación**

Agrega en `~/.gradle/gradle.properties`:

```properties
gpr.user=TU_USUARIO_GITHUB
gpr.token=TU_PERSONAL_ACCESS_TOKEN
```

**2. Agregar en tu `build.gradle`**:

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/GabFrank/rshk-jsifenlib")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation 'io.github.gabfrank:jsifenlib:0.2.4-frc.13'
}
```

## 🔄 Actualización de versiones

Para actualizar a una nueva versión:

1. **Verificar la última versión** en [Releases](https://github.com/GabFrank/rshk-jsifenlib/releases)
2. **Actualizar la versión** en tu `pom.xml` o `build.gradle`
3. **Ejecutar**:
   ```bash
   # Maven
   mvn clean install
   
   # Gradle
   ./gradlew build --refresh-dependencies
   ```

## 📖 Uso

***Importante: Leer el [Manual Técnico de Sifen](https://www.dnit.gov.py/documents/20123/420592/Manual+T%C3%A9cnico+Versi%C3%B3n+150.pdf) antes de continuar.***

### Configuración inicial

```java
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;

public class SifenSetup {
    
    public static void configurar() {
        SifenConfig config = new SifenConfig(
            SifenConfig.TipoAmbiente.PROD,  // o DEV para testing
            SifenConfig.TipoCertificadoCliente.PFX,
            "/ruta/al/certificado.pfx",
            "password_del_certificado"
        );
        
        // Con CSC (Código de Seguridad del Contribuyente)
        config.setCsc("ABCD0000000000000000000000000000");
        config.setCscId("0001");
        
        Sifen.setSifenConfig(config);
    }
}
```

### Configuración desde archivo properties

Crear `sifen.properties`:

```properties
sifen.ambiente=PROD
sifen.certificado_cliente.usar=true
sifen.certificado_cliente.tipo=PFX
sifen.certificado_cliente.archivo=/ruta/al/certificado.pfx
sifen.certificado_cliente.contrasena=password
sifen.csc=ABCD0000000000000000000000000000
sifen.csc.id=0001
sifen.habilitar_nota_tecnica_13=true
```

Cargar configuración:

```java
SifenConfig config = SifenConfig.loadFromFileName("sifen.properties");
Sifen.setSifenConfig(config);
```

### Ejemplo: Consulta de RUC

```java
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.beans.response.RespuestaConsultaRUC;

public class ConsultaRUCExample {
    
    public void consultarRUC() throws Exception {
        RespuestaConsultaRUC respuesta = Sifen.consultaRUC("80089752");
        
        if (respuesta.getCodigoEstado() == 200) {
            System.out.println("RUC: " + respuesta.getxContRUC().getdRUC());
            System.out.println("Razón Social: " + respuesta.getxContRUC().getdNombres());
        }
    }
}
```

### Ejemplo: Recepción de Evento (Cancelación)

```java
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.beans.EventosDE;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionEvento;
import com.roshka.sifen.core.fields.request.event.TrGesEve;

public class CancelacionExample {
    
    public void cancelarDocumento(String cdc) throws Exception {
        EventosDE eventosDE = new EventosDE();
        eventosDE.setdFecFirma(LocalDateTime.now());
        
        TrGesEve evento = new TrGesEve();
        evento.setrGeVeCan(/* configurar evento de cancelación */);
        
        eventosDE.setrGesEveList(Collections.singletonList(evento));
        
        // Ahora funciona correctamente sin XML mal formado
        RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);
        
        if (respuesta.getCodigoEstado() == 200) {
            System.out.println("Evento procesado exitosamente");
        }
    }
}
```

### Ejemplo: Spring Boot Service

```java
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;

@Service
public class SifenService {
    
    @Value("${sifen.certificado.path}")
    private String certificadoPath;
    
    @Value("${sifen.certificado.password}")
    private String certificadoPassword;
    
    @Value("${sifen.csc}")
    private String csc;
    
    @Value("${sifen.csc.id}")
    private String cscId;
    
    @PostConstruct
    public void init() {
        SifenConfig config = new SifenConfig(
            SifenConfig.TipoAmbiente.PROD,
            SifenConfig.TipoCertificadoCliente.PFX,
            certificadoPath,
            certificadoPassword
        );
        config.setCsc(csc);
        config.setCscId(cscId);
        
        Sifen.setSifenConfig(config);
    }
    
    public RespuestaConsultaRUC consultarRUC(String ruc) throws Exception {
        return Sifen.consultaRUC(ruc);
    }
    
    public RespuestaRecepcionEvento cancelarDocumento(EventosDE eventosDE) throws Exception {
        return Sifen.recepcionEvento(eventosDE);
    }
}
```

## 🔧 Servicios Web disponibles

- ✅ Consulta de RUC
- ✅ Recepción de Documento Electrónico (Síncrono)
- ✅ Consulta de Documentos Electrónicos
- ✅ Recepción de Lote de Documentos Electrónicos (Asíncrono)
- ✅ Consulta de Estado de Documentos Electrónicos (Lote)
- ✅ Recepción de Eventos (Cancelación, Inutilización, etc.)

## 🔄 Migración desde la versión original

Si estás usando `com.roshka.sifen:rshk-jsifenlib:0.2.4`:

**Antes:**
```xml
<dependency>
    <groupId>com.roshka.sifen</groupId>
    <artifactId>rshk-jsifenlib</artifactId>
    <version>0.2.4</version>
</dependency>
```

**Después:**
```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/GabFrank/rshk-jsifenlib</url>
    </repository>
</repositories>

<dependency>
    <groupId>io.github.gabfrank</groupId>
    <artifactId>jsifenlib</artifactId>
    <version>0.2.4-frc.13</version>
</dependency>
```

**No se requieren cambios en el código**, solo actualizar la dependencia y configurar GitHub Packages.

## 🐛 Troubleshooting

### Error 401 al descargar la dependencia

- Verifica que tu Personal Access Token tenga el scope `read:packages`
- Verifica que el `<id>` en `settings.xml` coincida con el `<id>` del repositorio en `pom.xml`
- Verifica que el token no haya expirado

### No encuentra la dependencia

```bash
# Limpiar cache de Maven
mvn dependency:purge-local-repository

# O forzar actualización
mvn clean install -U
```

### Ver logs detallados

```bash
mvn clean install -X
```

## 📝 Notas Técnicas

### Nota Técnica Nº 13 (23/04/2023)

Cambios en los campos de IVA. Para habilitarla:

```properties
sifen.habilitar_nota_tecnica_13=true
```

### Nota Técnica Nº 14

Aún no soportada en esta versión.

## 🤝 Contribución

Las contribuciones son bienvenidas. Por favor:

1. Crea un issue para discutir los cambios
2. Fork el proyecto
3. Crea una rama para tu feature
4. Envía un pull request

## 📄 Licencia

MIT License - Ver [LICENCIA.md](LICENCIA.md)

## 🔗 Enlaces

- [Proyecto original](https://github.com/roshkadev/rshk-jsifenlib)
- [Manual Técnico SIFEN](https://www.dnit.gov.py/documents/20123/420592/Manual+T%C3%A9cnico+Versi%C3%B3n+150.pdf)
- [Releases](https://github.com/GabFrank/rshk-jsifenlib/releases)
- [Issues](https://github.com/GabFrank/rshk-jsifenlib/issues)

## 👥 Autores

**Proyecto Original:**
- Pablo Santa Cruz ([github/pablo](https://github.com/pablo))
- Martin Zarza ([github/martinzarza](https://github.com/martinzarza))
- David Ayala ([github/david-ayala](https://github.com/david-ayala))

**Fork:**
- Gabriel Frank ([github/GabFrank](https://github.com/GabFrank))

---

⭐ Si este fork te fue útil, considera darle una estrella al repositorio

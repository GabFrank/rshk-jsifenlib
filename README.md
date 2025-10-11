# FRC jsifenlib (Fork de RSHK jsifenlib)

Este es un fork de `rshk-jsifenlib` con correcciones importantes para problemas de XML mal formado en eventos de cancelación.

## ¿Por qué este fork?

La versión 0.2.4 publicada en Maven Central tiene un problema donde `Sifen.recepcionEvento(eventosDE)` genera XML mal formado para operaciones de cancelación. Este fork incluye las correcciones más recientes del repositorio upstream que solucionan este problema.

## Instalación

### Maven

Primero, agrega el repositorio de GitHub Packages en tu `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/GabFrank/rshk-jsifenlib</url>
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

Luego, configura la autenticación en tu `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

**Nota**: Necesitas un [Personal Access Token de GitHub](https://github.com/settings/tokens) con el scope `read:packages`.

### Gradle

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

Agrega en tu `~/.gradle/gradle.properties`:

```properties
gpr.user=TU_USUARIO_GITHUB
gpr.token=TU_PERSONAL_ACCESS_TOKEN
```

## Servicios Web de SIFEN

- Consulta de RUC
- Recepción de Documento Electrónico - Síncrono
- Consulta de Documentos Electrónicos
- Recepción de Lote de Documentos Electrónicos - Asíncrono
- Consulta de Estado de Documentos Electrónicos (Lote)
- Recepción de Eventos

## Uso

***Importante: Leer
el [Manual Técnico de Sifen](https://www.dnit.gov.py/documents/20123/420592/Manual+T%C3%A9cnico+Versi%C3%B3n+150.pdf/e706f7c7-6d93-21d4-b45b-5d22d07b2d22?t=1687351495907)
y entender el funcionamiento básico antes de continuar con la guía de uso de esta librería.***

### Configuración

Para utilizar las funciones ofrecidas por `RSHK jsifenlib` es necesario configurar los siguientes ítems:

- Tipo de Ambiente (Desarrollo o Producción)
- Certificado y Tipo de Certificado (Solo PFX está soportado actualmente)
- Código de Seguridad (CSC)

Para esto, crear una nueva instancia de la clase `SifenConfig` utilizando alguno de los constructores:

```java

public class MiClase {

    public static void main(String[] args) {
        // Constructor vacío. Los valores se deben agregar por medio de los *setters* de la clase.
        SifenConfig config = new SifenConfig();

        // Constructor con el tipo de ambiente, tipo de certificado, la ruta del certificado y la contraseña.
        SifenConfig config = new SifenConfig(
                SifenConfig.TipoAmbiente.PROD,
                SifenConfig.TipoCertificadoCliente.PFX,
                "C:\\Users\\Roshka\\Documents\\certificado.pfx",
                "password"
        );

    }

}

public class MiClase {
    
    public static void main(String[] args) {
        // Constructor vacío. Los valores se deben agregar por medio de los *setters* de la clase.
        SifenConfig config = new SifenConfig();
        
        // Constructor igual al anterior, con el CSC y su ID.
        SifenConfig config = new SifenConfig(
                SifenConfig.TipoAmbiente.PROD,
                "0002", // ID CSC
                "EFGH0000000000000000000000000000", // CSC
                SifenConfig.TipoCertificadoCliente.PFX,
                "C:\\Users\\Roshka\\Documents\\certificado.pfx",
                "password"
        );
        
    }
        
}
```

La clase `SifenConfig` también se puede crear a partir de un método de construcción que toma los datos de un
archivo de propiedades de la siguiente manera:

```java
SifenConfig sifenConfig = SifenConfig.loadFromFileName("conf/sifen.properties");
```

Este archivo debe tener el siguiente formato, dependiendo de las necesidades:

```properties
# Esto puede ser:
# DEV (apunta al ambiente de desarrollo/test de SIFEN)
# PROD (apunta al ambiente de producción de SIFEN)
sifen.ambiente=DEV

# NO modificar a menos que uno sepa bien lo que está haciendo
# sifen.url_base=

# Si se va a usar el certificado cliente.
# Este valor en el 99.99% de las veces va a ser true
# a menos que uno sepa bien lo que está haciendo
sifen.certificado_cliente.usar=true
# Por ahora, el único valor aceptado es PFX
sifen.certificado_cliente.tipo=PFX

## RUTA APUNTANDO AL ARCHIVO PFX
sifen.certificado_cliente.archivo=/home/charly/garcia.pfx

## PASSWORD del ARCHIVO PFX
sifen.certificado_cliente.contrasena=my_password

## Nota técnica 13 (23/04/2023)
## Para habilitar los campos nuevos de esta nota técnica, cambiar a true
sifen.habilitar_nota_tecnica_13=false

## CSC
sifen.csc=ABCD0000000000000000000000000000
sifen.csc.id=0001
```
Luego de preparar la configuración, establecer la misma para usarla con las diferentes consultas.

```java
Sifen.setSifenConfig(config);
```

Esto solo debe realizarse una vez al principio, antes de ejecutar alguna acción. Si la configuración necesita ser
actualizada, simplemente invocar de vuelta.

### Consulta de RUC

El uso del servicio web de Consulta de RUC se puede realizar de la siguiente forma:

```java
RespuestaConsultaRUC respuesta = Sifen.consultaRUC("80089752");
```

El parámetro es el RUC del contribuyente a consultar, sin el DV (Dígito Verificador).

Para ver la estructura de la respuesta a esta consulta, revisar el Manual Técnico de Sifen, cuyo enlace se encuentra al
principio de esta sección.

## Nota Técnica Nº 13 (23/04/2023)

La Nota Técnica Nº 13 establece cambios en los campos de IVA de los documentos electrónicos. Las fechas de implementación de estos campos son las siguientes:

* Ambiente de desarrollo: 21/04/2023
* Ambiente de producción: 21/05/2023

Para esto se agrega una propiedad en el archivo de configuración `sifen.habilitar_nota_tecnica_13`. Esta propiedad es booleana,
si el valor es verdadero, entonces se agregará el nuevo elemento XML al documento electrónico correspondiente. Lo lógico sería
que este valor luego de la fecha de puesta en producción, sea *siempre* `true`.

## Nota Técnica Nº 14 (23/04/2023)

La Nota Técnica Nº 14 aun no está soportada en esta versión.

## Sugerencias

Si tenés alguna duda o consulta, o encontraste un comportamiento incorrecto dentro de la librería, no dudes en crear
un *issue*.

## Contribución

Cualquier contribución es siempre bienvenida. Por favor, crear primero un *issue* para discutir los cambios a ser
realizados.

## Licencia

`RSHK jsifenlib` está licenciada bajo el MIT License. Ver el archivo [LICENCIA.md](LICENCIA.md) para más detalles.

## Empresas usuarias de jsifenlib

Algunas empresas que utilizan esta librería en producción para sus implementaciones de SIFEN (*) son:

1. [Taxare S.A.](https://www.taxit.com.py) para su producto TAXit! (2021-05)
2. [NEXO S.A.E.C.A](http://www.nexo.com.py) (2022-03)
3. [Roshka S.A.](https://www.roshka.com) (2022-04)
4. [SIFAMERICA S.A.](https://www.sif.com.py) (2022-11)
5. [Despachos Aduaneros Cacavelos](http://www.despachoscacavelos.com.py/) (2022-11)
6. [Biggie S.A.](https://www.biggie.com.py/) (2023-01)
7. [Banco de la Nación Argentina](https://www.bna.com.py/) (2023-01)
8. [VIDRIO LUZ SRL](http://www.vidrioluz.com.py/web/) (2023-01)
9. [UNION SRL](http://www.unionsrl.com.py/) (2023-04)

(*) ¿Querés agregar la tuya? Envianos un [email](mailto:pablo@roshka.com.py) para incluirla. 

## Autores y Responsables

- Pablo Santa Cruz ([github/pablo](https://github.com/pablo))
- Martin Zarza ([github/martinzarza](https://github.com/martinzarza))
- David Ayala ([github/david-ayala](https://github.com/david-ayala))

## Diferencias con el proyecto original

- ✅ **Dependencias SOAP incluidas**: `saaj-impl:1.5.3` y `javax.xml.soap-api:1.4.0`
- ✅ **XML bien formado**: Corrige el problema de XML mal formado en `recepcionEvento()`
- ✅ **Sincronización regular**: Este fork se mantiene actualizado con el proyecto upstream

## Migración desde la versión original

Si estás usando la versión original y tienes problemas con XML mal formado en eventos de cancelación:

```xml
<!-- Reemplazar esto -->
<dependency>
    <groupId>com.roshka.sifen</groupId>
    <artifactId>rshk-jsifenlib</artifactId>
    <version>0.2.4</version>
</dependency>

<!-- Por esto -->
<dependency>
    <groupId>io.github.gabfrank</groupId>
    <artifactId>jsifenlib</artifactId>
    <version>0.2.4-frc.3</version>
</dependency>
```

No se requieren cambios en el código, solo actualizar la dependencia.

## Sincronización con upstream

Este fork se mantiene sincronizado con el [proyecto original](https://github.com/roshkadev/rshk-jsifenlib). Cuando el proyecto original publique una nueva versión con estos fixes, se evaluará la migración de vuelta al proyecto principal.

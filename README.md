# ASTM Paint Evaluator - Android App & ML Pipeline

Aplicación Android para evaluar grado de oxidación (ASTM D610) y ampollamiento (ASTM D714) en paneles pintados, con aprendizaje activo mediante Google Drive y Colab.

## Arquitectura del Sistema

```
┌─────────────────┐      ┌──────────────────┐      ┌─────────────────┐
│   App Android   │─────▶│  Google Drive    │─────▶│  Google Colab   │
│                 │      │                  │      │                 │
│ - CameraX       │      │ /To_Train/       │      │ - Entrenamiento │
│ - TFLite        │      │ /Models/         │      │ - Exportación   │
│ - UI Compose    │      │                  │      │   .tflite       │
│                 │◀─────│                  │◀─────│                 │
└─────────────────┘      └──────────────────┘      └─────────────────┘
       ▲                                                   │
       │                                                   │
       └───────────────────────────────────────────────────┘
                    (Descarga automática del modelo)
```

## Estructura del Proyecto

```
/workspace/
├── AndroidApp/                      # Aplicación Android completa
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/tuempresa/astm_evaluator/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── CameraScreen.kt
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   └── ResultScreen.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── PreferencesManager.kt
│   │   │   │   │   └── DriveManager.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── AstmClassifier.kt
│   │   │   │   │   └── AstmDataClasses.kt
│   │   │   │   └── utils/
│   │   │   │       └── Constants.kt
│   │   │   ├── assets/models/
│   │   │   │   └── astm_model.tflite
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── ColabNotebooks/
│   └── train_astm_model.ipynb      # Notebook para entrenar en Colab
│
├── DriveStructure/
│   └── README.txt                   # Guía de organización de carpetas en Drive
│
└── README.md                        # Este archivo
```

## Paso a Paso: Implementación Completa

### Fase 1: Configurar Google Drive

1. **Crear estructura de carpetas en tu Google Drive:**

```
/ASTM_Dataset/
├── /To_Train/              # Aquí la app sube fotos corregidas
│   ├── /Rust/
│   └── /Blister/
├── /Models/                # Aquí Colab guarda los modelos entrenados
│   └── /v1/
│   └── /v2/
└── /Reference_Images/      # Imágenes de ejemplo de las normas ASTM
```

2. **Habilitar Google Drive API:**
   - Ve a [Google Cloud Console](https://console.cloud.google.com/)
   - Crea un nuevo proyecto
   - Habilita "Google Drive API"
   - Crea credenciales OAuth 2.0 (tipo "Android")
   - Descarga el archivo `client_secret.json`

### Fase 2: Compilar e Instalar la App Android

#### Requisitos Previos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- SDK de Android 34 (API Level 34)
- Dispositivo Android físico o emulador (minSdk 24)

#### Pasos de Compilación

1. **Abrir el proyecto en Android Studio:**
   ```bash
   # Desde Android Studio: File > Open > /workspace/AndroidApp
   ```

2. **Sincronizar dependencias:**
   - Android Studio sincronizará automáticamente Gradle
   - Verifica que todas las dependencias se descarguen correctamente

3. **Configurar Google Drive en la app:**
   - Abre `AndroidApp/app/src/main/res/values/strings.xml`
   - Reemplaza `YOUR_GOOGLE_CLIENT_ID` con tu Client ID de Google Cloud
   
   ```xml
   <string name="google_client_id">YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com</string>
   ```

4. **Colocar modelo inicial (opcional):**
   - Si ya tienes un modelo `.tflite`, colócalo en:
     `AndroidApp/app/src/main/assets/models/astm_model.tflite`
   - Si no, la app funcionará en modo "demo" hasta que entrenes el primero

5. **Compilar y ejecutar:**
   - Click en **Build > Build Bundle(s) / APK(s) > Build APK(s)**
   - O usa **Run > Run 'app'** para instalar directamente en el dispositivo
   
   ```bash
   # También puedes usar command line:
   cd /workspace/AndroidApp
   ./gradlew assembleDebug
   # El APK estará en: app/build/outputs/apk/debug/app-debug.apk
   ```

6. **Instalar en el dispositivo:**
   - Conecta tu Android vía USB (con depuración USB activada)
   - Ejecuta:
     ```bash
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```

### Fase 3: Primer Uso de la App

1. **Configurar dimensiones del panel:**
   - Abre la app
   - Ve a **Configuración**
   - Ingresa ancho y alto del panel en mm (ej: 70 x 150)
   - Guarda

2. **Capturar primeras fotos:**
   - Alinea el panel dentro del marco guía verde
   - La app verifica automáticamente iluminación y enfoque
   - Presiona **CAPTURAR**

3. **Validar/Corregir predicciones:**
   - La app mostrará una predicción inicial (puede ser incorrecta al principio)
   - Usa los controles manuales para ajustar:
     - **Óxido (D610):** Grado 0-10 + Distribución (S/G/P/H)
     - **Ampollas (D714):** Tamaño (10/8/6/4/2) + Frecuencia (F/M/MD/D)
   - Si corriges la predicción, la foto se marca para reentrenamiento

4. **Subida automática a Drive:**
   - Las fotos corregidas se suben automáticamente a `/ASTM_Dataset/To_Train/`
   - La app lleva un contador interno

### Fase 4: Entrenar Modelo en Google Colab

1. **Abrir el notebook:**
   - Ve a [Google Colab](https://colab.research.google.com/)
   - Sube el archivo `ColabNotebooks/train_astm_model.ipynb`
   - O ábrelo directamente desde tu Google Drive

2. **Ejecutar celdas en orden:**
   ```python
   # 1. Montar Google Drive
   from google.colab import drive
   drive.mount('/content/drive')
   
   # 2. Configurar rutas
   DATASET_DIR = "/content/drive/MyDrive/ASTM_Dataset/To_Train"
   MODEL_OUTPUT_DIR = "/content/drive/MyDrive/ASTM_Dataset/Models/v1"
   
   # 3. Cargar y preprocesar imágenes
   # (El notebook hace data augmentation automáticamente)
   
   # 4. Entrenar modelo (Transfer Learning con MobileNetV2)
   # Tiempo estimado: 15-30 minutos en GPU gratuita
   
   # 5. Exportar a TensorFlow Lite
   # El archivo se guarda en: MODEL_OUTPUT_DIR/astm_model_v1.tflite
   ```

3. **Descargar modelo entrenado:**
   - El notebook guarda automáticamente el `.tflite` en `/ASTM_Dataset/Models/v1/`
   - Cada versión se guarda con timestamp: `astm_model_v{version}_{timestamp}.tflite`

### Fase 5: Actualizar Modelo en la App

La app está diseñada para **descargar automáticamente** el modelo más reciente al iniciar:

1. **Al iniciar la app:**
   - Verifica si hay un nuevo modelo en `/ASTM_Dataset/Models/` en Google Drive
   - Compara versión local vs. remota
   - Si hay una versión nueva, la descarga en segundo plano

2. **Actualización manual (si es necesaria):**
   - Ve a **Configuración > Actualizar Modelo**
   - La app descarga el último `.tflite` disponible
   - Reinicia el clasificador con el nuevo modelo

3. **¡Listo!** No necesitas recompilar la app. El nuevo modelo se carga automáticamente.

### Fase 6: Ciclo de Mejora Continua

```
┌─────────────────────────────────────────────────────────────┐
│                    CICLO DE APRENDIZAJE                     │
├─────────────────────────────────────────────────────────────┤
│ 1. Usuario toma fotos y corrige predicciones en la app      │
│ 2. Fotos corregidas se suben a Drive (/To_Train/)           │
│ 3. Cuando hay 50+ fotos nuevas, la app notifica             │
│ 4. Usuario ejecuta notebook en Colab                        │
│ 5. Colab entrena nuevo modelo y lo guarda en /Models/       │
│ 6. Al reiniciar, la app descarga el nuevo modelo            │
│ 7. La app ahora tiene mejor precisión                       │
│ 8. Volver al paso 1                                         │
└─────────────────────────────────────────────────────────────┘
```

## Normas ASTM Implementadas

### ASTM D610-08 (2012) - Óxido en Superficies Pintadas
- **Grados:** 0 a 10 (basado en porcentaje de área oxidada)
- **Distribuciones:**
  - **S** (Spot): Concentrado en áreas localizadas
  - **G** (General): Distribuido aleatoriamente
  - **P** (Pinpoint): Pequeños puntos individuales
  - **H** (Hybrid): Combinación de tipos

| Grado | % Área Oxidada |
|-------|----------------|
| 10    | ≤ 0.01%        |
| 9     | 0.01% - 0.03%  |
| 8     | 0.03% - 0.1%   |
| 7     | 0.1% - 0.3%    |
| 6     | 0.3% - 1.0%    |
| 5     | 1.0% - 3.0%    |
| 4     | 3.0% - 10.0%   |
| 3     | 10.0% - 16.0%  |
| 2     | 16.0% - 33.0%  |
| 1     | 33.0% - 50.0%  |
| 0     | > 50.0%        |

### ASTM D714-17 - Ampollamiento en Pinturas
- **Tamaños:** 10, 8, 6, 4, 2 (10 = sin ampollas, 2 = más grandes)
- **Frecuencias:**
  - **F** (Few): Pocas ampollas
  - **M** (Medium): Densidad media
  - **MD** (Medium Dense): Media-densa
  - **D** (Dense): Muy densas

## Solución de Problemas

### La app no sube fotos a Drive
- Verifica que hayas iniciado sesión con tu cuenta de Google
- Confirma que los permisos de Drive estén otorgados
- Revisa conexión a internet

### El modelo no se actualiza automáticamente
- Ve a **Configuración > Actualizar Modelo** forzado
- Verifica que el archivo `.tflite` exista en `/ASTM_Dataset/Models/` en Drive
- Revisa logs de la app (Activar modo desarrollador en configuración)

### Errores de compilación en Android Studio
- Ejecuta: `./gradlew clean` en la carpeta del proyecto
- Invalida cachés: **File > Invalidate Caches / Restart**
- Verifica que JDK 17 esté configurado en **File > Project Structure > SDK Location**

### El entrenamiento en Colab falla por memoria
- Reduce el batch size en el notebook (de 32 a 16 o 8)
- Usa "Disconnect and delete runtime" y vuelve a conectar para obtener una GPU fresca
- Asegúrate de estar usando la GPU gratuita (**Runtime > Change runtime type > GPU**)

## Próximos Pasos Sugeridos

1. **Recolectar dataset inicial:** Toma 100-200 fotos de paneles con diferentes grados de defectos
2. **Entrenar primer modelo:** Ejecuta Colab con este dataset base
3. **Desplegar en campo:** Instala la app en dispositivos de inspectores
4. **Monitorear precisión:** Revisa cuántas correcciones manuales se hacen
5. **Iterar:** Repite el ciclo de entrenamiento cada semana con nuevas datos

## Licencia

Este proyecto es de uso interno para evaluación de paneles pintados según normas ASTM.

## Contacto

Para preguntas sobre implementación, contactar al equipo de desarrollo.
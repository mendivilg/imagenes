# Estructura de Carpetas para Google Drive

## Directorio Principal: /ASTM_Dataset/

### 1. /To_Train/ - Dataset para Entrenamiento
Aquí la app Android sube automáticamente las fotos que el usuario corrigió manualmente.

```
/ASTM_Dataset/To_Train/
├── /Rust_10/           # Grado 10 (≤ 0.01% óxido)
├── /Rust_9_S/          # Grado 9, distribución Spot
├── /Rust_9_G/          # Grado 9, distribución General
├── /Rust_9_P/          # Grado 9, distribución Pinpoint
├── /Rust_8_S/          # Grado 8, distribución Spot
├── /Rust_8_G/          # ...
├── /Rust_8_P/
├── /Rust_7_S/
├── /Rust_7_G/
├── /Rust_7_P/
├── /Rust_6_S/
├── /Rust_6_G/
├── /Rust_6_P/
├── /Rust_5_S/
├── /Rust_5_G/
├── /Rust_5_P/
├── /Rust_4_S/
├── /Rust_4_G/
├── /Rust_4_P/
├── /Rust_3_S/
├── /Rust_3_G/
├── /Rust_3_P/
├── /Rust_2_S/
├── /Rust_2_G/
├── /Rust_2_P/
├── /Rust_1_S/
├── /Rust_1_G/
├── /Rust_1_P/
├── /Rust_0/            # Grado 0 (> 50% óxido)
│
├── /Blister_10/        # Sin ampollas
├── /Blister_8_F/       # Tamaño 8, frecuencia Few
├── /Blister_8_M/       # Tamaño 8, frecuencia Medium
├── /Blister_8_MD/      # Tamaño 8, frecuencia Medium Dense
├── /Blister_8_D/       # Tamaño 8, frecuencia Dense
├── /Blister_6_F/
├── /Blister_6_M/
├── /Blister_6_MD/
├── /Blister_6_D/
├── /Blister_4_F/
├── /Blister_4_M/
├── /Blister_4_MD/
├── /Blister_4_D/
├── /Blister_2_F/
├── /Blister_2_M/
├── /Blister_2_MD/
└── /Blister_2_D/
```

**Nota:** La app Android crea automáticamente estas carpetas según sea necesario.

### 2. /Models/ - Modelos Entrenados
Aquí el notebook de Colab guarda los modelos .tflite resultantes del entrenamiento.

```
/ASTM_Dataset/Models/
├── latest.tflite                    # Último modelo (la app descarga este)
├── astm_model_v1_20240101_120000.tflite
├── astm_model_v2_20240115_143000.tflite
├── astm_model_v3_20240201_091500.tflite
├── classes_v1.txt
├── classes_v2.txt
└── classes_v3.txt
```

**Importante:** 
- El archivo `latest.tflite` es una copia del modelo más reciente
- La app Android solo descarga `latest.tflite` automáticamente
- Los modelos versionados se mantienen como backup

### 3. /Reference_Images/ - Imágenes de Referencia (Opcional)
Copia digital de las normas ASTM para consulta rápida.

```
/ASTM_Dataset/Reference_Images/
├── ASTM_D610_Rust_Chart.jpg
├── ASTM_D714_Blister_Chart.jpg
└── Example_Photos/
    ├── Rust_Grade_10_Example.jpg
    ├── Rust_Grade_5_Example.jpg
    └── Blister_Size_8_Example.jpg
```

---

## Flujo de Datos

```
┌──────────────┐     Sube fotos corregidas     ┌─────────────────┐
│              │ ───────────────────────────▶  │  /To_Train/     │
│  App Android │                               │  (Dataset)      │
│              │ ◀───────────────────────────  │                 │
└──────────────┘     Descarga latest.tflite    └────────┬────────┘
       ▲                                                │
       │                                                │ Ejecuta notebook
       │                                                ▼
       │                                       ┌─────────────────┐
       │                                       │   Google Colab  │
       │                                       │                 │
       │                                       │ 1. Lee /To_Train/
       │                                       │ 2. Entrena modelo
       │                                       │ 3. Guarda en /Models/
       │                                       └─────────────────┘
       │
       └─────────────────────────────────────────────────┘
                  Actualización automática al iniciar
```

---

## Configuración Inicial Requerida

Antes de usar la app por primera vez:

1. **Crear carpeta principal en tu Google Drive:**
   - Nombre: `ASTM_Dataset`
   
2. **Dentro de ASTM_Dataset, crear tres subcarpetas:**
   - `To_Train`
   - `Models`
   - `Reference_Images` (opcional)

3. **La app creará automáticamente** las subcarpetas específicas (Rust_10, Blister_8_F, etc.) según sea necesario.

4. **Colocar un modelo inicial** (opcional):
   - Si ya tienes un archivo `.tflite`, colócalo en `/Models/latest.tflite`
   - Si no, la app funcionará en modo demo hasta que entrenes el primer modelo

---

## Permisos de Google Drive

La app solicitará permisos para:
- ✅ Leer archivos de `/Models/` (para descargar el modelo)
- ✅ Escribir archivos en `/To_Train/` (para subir fotos corregidas)
- ✅ Listar directorios (para verificar estructura)

**Nota:** La app NO tiene acceso a otras carpetas de tu Drive.

---

## Mantenimiento

### Cuando el contador de reentrenamiento llegue a 50:
1. La app mostrará una notificación
2. Abre Google Colab y ejecuta el notebook `train_astm_model.ipynb`
3. Espera a que finalice el entrenamiento (~15-30 minutos)
4. El notebook guardará automáticamente el nuevo modelo en `/Models/`
5. Reinicia la app para que descargue el nuevo modelo
6. El contador se reseteará automáticamente

### Limpieza periódica (recomendado cada 3 meses):
- Revisar `/To_Train/` y eliminar duplicados obvios
- Mover modelos antiguos a una carpeta de backup
- Mantener solo las últimas 5 versiones de modelos

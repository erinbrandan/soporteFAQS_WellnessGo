# 🌐 WellnessGo – Portal de Soporte y FAQs

Este repositorio contiene el **código fuente del portal web de atención al cliente de WellnessGo**.  
Se trata de una **plataforma web estática**, optimizada para su correcta visualización tanto en **dispositivos móviles** como en **escritorio**, diseñada para resolver las dudas más frecuentes de los usuarios de la aplicación Android.

---

## 🚀 Despliegue y Alojamiento

La web se encuentra actualmente **desplegada en un servidor remoto en Clouding**, lo que garantiza:

- Alta disponibilidad
- Tiempos de respuesta rápidos

Los usuarios acceden al portal directamente desde la aplicación móvil a través de la sección **Soporte**.

---

## 🛠️ Tecnologías Utilizadas

- **HTML5**  
  Estructura semántica utilizando elementos como `<details>` y `<summary>` para crear un sistema de acordeón nativo y accesible.

- **CSS3**  
  - Diseño personalizado con gradientes alineados con la identidad visual de la app móvil  
  - Uso de **Flexbox** para la disposición de elementos y formularios

- **IntelliJ IDEA**  
  Entorno de desarrollo utilizado para la codificación y gestión del proyecto.

---

## 📋 Funcionalidades Principales

### 🧠 Centro de Ayuda (FAQs)
Interfaz interactiva donde los usuarios pueden consultar dudas sobre:

- Gestión de citas y cancelaciones
- Actualización del perfil
- Recuperación de credenciales

### ✉️ Formulario de Contacto Directo
Integración mediante el protocolo `mailto`, que permite a los usuarios enviar consultas personalizadas al equipo de administración:
admin@wellnessgo.com
### 📱 Diseño Responsive
La interfaz está completamente adaptada para evitar rupturas visuales al pasar de la aplicación nativa al navegador del dispositivo móvil.

---

### 📂 Estructura del Proyecto

* `index.html`: Estructura principal, contenido de las preguntas frecuentes y formulario de contacto.
* `styles.css`: Hoja de estilos que define la paleta de colores corporativa (Cian `#00a9b8` y Verde oscuro `#006c58`) y la experiencia visual.
* `/img`: Recursos gráficos y logotipos de la marca.

### 🎨 Paleta de colores corporativa

* **Cian:** `#00a9b8`
* **Verde oscuro:** `#006c58`


## 🔗 Integración con la App

Este portal es el destino del enlace externo configurado en la clase `Soporte.java` de la aplicación Android.  
Gracias a esta integración, el contenido de ayuda puede actualizarse sin necesidad de publicar una nueva versión de la app en la tienda.

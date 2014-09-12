# Factora

Factora es un conjunto de librerías en Clojure para la emisión de comprobantes electrónicos para el Servicio de Rentas Internas de Ecuador.

## Cómo empezar

1. Ejecuta `lein test` para verificar las pruebas. Revisa los tests en /test/factora/core_test.clj.
2. Revisa el código fuente del API en /src/factora/core.clj.
3. Lee la documentación en /doc
4. Conoce más de facturación electrónica en los [enlaces de las referencias](#referencias).

## Flujo de emision

Para emitir un comprobante electronico, se deben seguir los siguientes pasos de acuerdo a la [ficha tecnica del SRI](http://www.sri.gob.ec/web/10138/10044):

1. Generar de un XML valido del comprobante, conforme a los esquemas definidos por el SRI.
2. Firmar el XML conforme al estandard de firma definido por el SRI.
3. Codificar el XML en formato base64.
4. Enviar los datos al servicio web de revision del SRI. Este puede aprobar o rechazar el comprobante enviado.
5. Enviar los datos al servicio web de autorizacion del SRI.

## Tareas

* Generar tests para retenciones, notas de débito, notas de crédito y guías de remisión.
* Mejorar el tiempo de ejecución de las pruebas que involucran validación XSD.
* Incorporar una interface que permita adaptar varios módulos firmadores.
* Incorporar un cliente SOAP de los servicios web del SRI.
* Incorporar un motor de colas para gestionar el flujo de emisión.
* Incorporar una base de datos para registrar la actividad de la plataforma.
* Incorporar ejemplos y casos de uso.
* Incorporar una guía de contribución.

## Referencias
* [Esquema de Emisión de Comprobantes Electrónicos](http://www.sri.gob.ec/web/guest/facturacion-electronica1) del Servicio de Rentas Internas de Ecuador

## Licencia

Copyright &copy; 2014 Datilmedia S.A.

Los términos de uso y distribución de este software están cubiertos por la licencia [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), que puede ser encontrada en el archivo LICENSE de este repositorio.

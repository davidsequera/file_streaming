La aplicación genera un archivo de log en un directorio llamado logs. 
Se debe generar un archivo de log por cada prueba realizada y este debe contener registros para cada conexión 
recibida en la prueba. 
El formato de los logs es el siguiente:
a. El nombre del archivo de Logs debe incluir la fecha exacta de la prueba. Ejemplo: <año-mes-dia-hora-minuto-segundo-log.txt>
b. Incluya el nombre del archivo enviado y su tamaño.
c. Identifique para cada conexión el cliente al que se realiza la transferencia de archivos.
d. Identifique si la entrega del archivo fue exitosa o no. 
e. Tome los tiempos de transferencia a cada uno de los clientes, calcule este tiempo desde el 
momento que se recibe el primer paquete del archivo y hasta que se envía la confirmación 
de recepción del último paquete del archivo.
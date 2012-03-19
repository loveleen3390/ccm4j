
Falta considerar costo adicional de try - catch ?

Si invoco a un metodo de mi superclass, o mas arriba en la jerarquia,  se suma el costo o no?
(para las invocaciones a this() no se suman..)


Falta testear bien las llamadas recursivas que se hacen desde las implementaciones de metodos
abstractos. (Ej: composite, llamando a al metodo abstracto base del composite)

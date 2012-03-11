OJO:
		Invocaciones recursivas a traves de subclases/interfaces, no funcionan.
			Se podrian llegar a resolver si cuando se calculan las dependencias
			se tienen en cuenta la jerarquia
			
			
Las subclases se multiplican,  si el costo de una Subclase es 0 (no attributos ni methodos, raro)
		toda esa rama se anula
		
		
Falta haacer el promedio para los metodos abstractos.

Falta considerar costo adicional de try - catch ?

Falta tener en cuenta el costo de los constructores?

Si invoco a un metodo de mi superclass, o mas arriba en la jerarquia,  se suma el costo o no?
(para las invocaciones a this() no se suman..)


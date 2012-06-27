interface I {
    method()
}

abstract class B {
	method();
}

class A extends B implements I {

}

no encuentra a A como implementacion del methodo method() de la interfaz I ..
como hacer?..  
Si esta -> listo
Si no, buscar para arriba hasta object, si lo encuentro -> listo
Si no, si es abstracto, ok.   Si no es abstracto, ERROR!.


No encuentra tampoco las implementaciones por clases declaradas anonimas:

new EventListener() {
   void listen(Event evt) { ..}
}


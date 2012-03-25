Llamadas a constructores:  no cuenta nada cuando son llamadas a constructores implicitos.


interface I {
    method()
}

abstract class B {
	method();
}

class A extends B implements I {

}

El no encuentra a A como implementacion del methodo method() de la interfaz I ..
como hacer?..  
Si esta -> listo
Si no, buscar para arriba hasta object, si lo encuentro -> listo
Si no, si es abstracto, ok.   Si no es abstracto, ERROR!.
Warnning org.apache.xerces.xni.parser.XMLParserConfiguration.addRecognizedProperties(java.lang.String[]) doesn't have implementations
Warnning org.apache.xerces.xni.parser.XMLParserConfiguration.addRecognizedFeatures(java.lang.String[]) doesn't have implementations
Warnning org.apache.xerces.xni.parser.XMLParserConfiguration.addRecognizedProperties(java.lang.String[]) doesn't have implementations
Warnning org.apache.xerces.impl.dtd.XMLContentSpec.Provider.getContentSpec(int,org.apache.xerces.impl.dtd.XMLContentSpec) doesn't have implementations
Warnning org.apache.xerces.xni.parser.XMLParserConfiguration.addRecognizedProperties(java.lang.String[]) doesn't have implementations
Warnning org.apache.xerces.impl.xs.traversers.Container.get(java.lang.String) doesn't have implementations
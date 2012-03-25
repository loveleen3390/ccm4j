package ar.edu.unicen.ccm.bcs.abstractmethod;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;

public interface MethodWeightStrategy {
	public BigInteger weight(Collection<BigInteger> implWeights, StringBuilder builder);

}

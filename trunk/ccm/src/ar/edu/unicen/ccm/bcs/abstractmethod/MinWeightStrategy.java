package ar.edu.unicen.ccm.bcs.abstractmethod;

import java.math.BigInteger;
import java.util.Collection;

public class MinWeightStrategy implements MethodWeightStrategy {

	@Override
	public BigInteger weight(Collection<BigInteger> implWeights,
			StringBuilder builder) {
		BigInteger min = null;
		for(BigInteger i : implWeights)
			if (min == null)
				min = i;
			else
				min = min.min(i);

		if (min == null)
			min = BigInteger.valueOf(0); //patological case
		builder.append("{").append(min).append("}");
		return min;	
		}

}

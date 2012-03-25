package ar.edu.unicen.ccm.bcs.abstractmethod;

import java.math.BigInteger;
import java.util.Collection;

public class MaxWeightStrategy implements MethodWeightStrategy {

	@Override
	public BigInteger weight(Collection<BigInteger> implWeights,
			StringBuilder builder) {
		BigInteger max = BigInteger.valueOf(0);
		for(BigInteger i : implWeights)
			max = max.max(i);

		builder.append("{").append(max).append("}");
		return max;
	}

}

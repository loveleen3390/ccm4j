package ar.edu.unicen.ccm.bcs.abstractmethod;

import java.math.BigInteger;
import java.util.Collection;

public class SumWeightStrategy implements MethodWeightStrategy {

	@Override
	public BigInteger weight(Collection<BigInteger> implWeights,
			StringBuilder builder) {
		BigInteger result = BigInteger.valueOf(0);
		builder.append("{");
		for (BigInteger i : implWeights) {
			builder.append("+" + i);
			result = result.add(i);
		}
		builder.append("}");
		return result;
	}

}

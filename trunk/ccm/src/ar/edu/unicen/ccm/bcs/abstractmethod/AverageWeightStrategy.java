package ar.edu.unicen.ccm.bcs.abstractmethod;

import java.math.BigInteger;
import java.util.Collection;

public class AverageWeightStrategy implements MethodWeightStrategy{
	@Override
	public BigInteger weight(Collection<BigInteger> implWeights,
			StringBuilder builder) {
		builder.append("{");
		BigInteger accum = BigInteger.valueOf(0);
		for (BigInteger i : implWeights) {
			builder.append("+").append(i);
			accum = accum.add(i);
		}
		builder.append("}/").append(implWeights.size());
		
		return accum.divide(BigInteger.valueOf(implWeights.size()));
	}

}

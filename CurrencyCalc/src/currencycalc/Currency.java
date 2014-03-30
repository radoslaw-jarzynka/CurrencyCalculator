package currencycalc;

public class Currency {

	String name;
	double value;
	double multiplier;

	public Currency(String name, double value, double multiplier) {
		super();
		this.name = name;
		this.value = value;
		this.multiplier = multiplier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public String toString() {
		return "Currency [name=" + name + ", value=" + value + ", multiplier="
				+ multiplier + "]";
	}
	
	public static Currency getCurrencyFromSerializedString(String str) {
		String[] _splitted = str.split("=,");
		String _name = _splitted[1];
		double _value = Double.parseDouble(_splitted[3]);
		String[] _multiplierStr = _splitted[5].split("]");
		double _multiplier = Double.parseDouble(_multiplierStr[0]);
		Currency _currency = new Currency(_name, _value, _multiplier);
		return _currency;
	}

}

/*
 * Copyright (c) 2010 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitlibrary.tutorial;

import fitlibrary.traverse.function.Rule;

public class DiscountRule implements Rule {
	private double givenAmount;

	public void setGivenAmount(double givenAmount) {
		this.givenAmount = givenAmount;
	}
	public double getExpectedDiscount() {
		if (givenAmount > 1000.00)
			return givenAmount * 0.05;
		return 0.00;
	}
}
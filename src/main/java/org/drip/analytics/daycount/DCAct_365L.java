
package org.drip.analytics.daycount;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2017 Lakshmi Krishnamurthy
 * Copyright (C) 2016 Lakshmi Krishnamurthy
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for buy/side financial/trading model
 *  	libraries targeting analysts and developers
 *  	https://lakshmidrip.github.io/DRIP/
 *  
 *  DRIP is composed of four main libraries:
 *  
 *  - DRIP Fixed Income - https://lakshmidrip.github.io/DRIP-Fixed-Income/
 *  - DRIP Asset Allocation - https://lakshmidrip.github.io/DRIP-Asset-Allocation/
 *  - DRIP Numerical Optimizer - https://lakshmidrip.github.io/DRIP-Numerical-Optimizer/
 *  - DRIP Statistical Learning - https://lakshmidrip.github.io/DRIP-Statistical-Learning/
 * 
 *  - DRIP Fixed Income: Library for Instrument/Trading Conventions, Treasury Futures/Options,
 *  	Funding/Forward/Overnight Curves, Multi-Curve Construction/Valuation, Collateral Valuation and XVA
 *  	Metric Generation, Calibration and Hedge Attributions, Statistical Curve Construction, Bond RV
 *  	Metrics, Stochastic Evolution and Option Pricing, Interest Rate Dynamics and Option Pricing, LMM
 *  	Extensions/Calibrations/Greeks, Algorithmic Differentiation, and Asset Backed Models and Analytics.
 * 
 *  - DRIP Asset Allocation: Library for model libraries for MPT framework, Black Litterman Strategy
 *  	Incorporator, Holdings Constraint, and Transaction Costs.
 * 
 *  - DRIP Numerical Optimizer: Library for Numerical Optimization and Spline Functionality.
 * 
 *  - DRIP Statistical Learning: Library for Statistical Evaluation and Machine Learning.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * This class implements the Act/365L day count convention.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DCAct_365L implements org.drip.analytics.daycount.DCFCalculator {

	/**
	 * Empty DCAct_365L constructor
	 */

	public DCAct_365L()
	{
	}

	@Override public java.lang.String baseCalculationType()
	{
		return "DCAct_365L";
	}

	@Override public java.lang.String[] alternateNames()
	{
		return new java.lang.String[] {"Act/365L", "Actual/365L", "ISMA-Year", "Actual/Actual AFB",
			"DCAct_365L"};
	}

	@Override public double yearFraction (
		final int iStartDate,
		final int iEndDate,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == actactParams)
			throw new java.lang.Exception ("DCAct_365L::yearFraction => Invalid actact Params!");

		DateEOMAdjustment dm = DateEOMAdjustment.MakeDEOMA (iStartDate, iEndDate, bApplyEOMAdj);

		if (null == dm)
			throw new java.lang.Exception ("DCAct_365L::yearFraction => Cannot create DateEOMAdjustment!");

		if (1 == actactParams.freq()) {
			if (org.drip.analytics.date.DateUtil.ContainsFeb29 (iStartDate, iEndDate,
				org.drip.analytics.date.DateUtil.RIGHT_INCLUDE))
				return 1. * (iEndDate - iStartDate + dm.posterior() - dm.anterior()) / 366.;

			return 1. * (iEndDate - iStartDate + dm.posterior() - dm.anterior()) / 365.;
		}

		if (org.drip.analytics.date.DateUtil.IsLeapYear (iEndDate))
			return 1. * (iEndDate - iStartDate + dm.posterior() - dm.anterior()) / 366.;

		return 1. * (iEndDate - iStartDate + dm.posterior() - dm.anterior()) / 365.;
	}

	@Override public int daysAccrued (
		final int iStartDate,
		final int iEndDate,
		final boolean bApplyEOMAdj,
		final ActActDCParams actactParams,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == actactParams)
			throw new java.lang.Exception ("DCAct_365L::daysAccrued => Invalid actact Params!");

		DateEOMAdjustment dm = DateEOMAdjustment.MakeDEOMA (iStartDate, iEndDate, bApplyEOMAdj);

		if (null == dm)
			throw new java.lang.Exception ("DCAct_365L::daysAccrued => Cannot create DateEOMAdjustment!");

		if (1 == actactParams.freq()) {
			if (org.drip.analytics.date.DateUtil.ContainsFeb29 (iStartDate, iEndDate,
				org.drip.analytics.date.DateUtil.RIGHT_INCLUDE))
				return iEndDate - iStartDate + dm.posterior() - dm.anterior();

			return iEndDate - iStartDate + dm.posterior() - dm.anterior();
		}

		return iEndDate - iStartDate + dm.posterior() - dm.anterior();
	}
}

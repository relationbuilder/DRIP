
package org.drip.sample.credit;

/*
 * Credit Product import
 */

import org.drip.analytics.cashflow.*;
import org.drip.analytics.date.*;
import org.drip.analytics.daycount.Convention;
import org.drip.param.market.CurveSurfaceQuoteContainer;
import org.drip.param.pricer.CreditPricerParams;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.env.EnvManager;
import org.drip.state.creator.*;
import org.drip.state.credit.CreditCurve;
import org.drip.state.discount.MergedDiscountForwardCurve;

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
 * CDSCashFlowMeasures contains a demo of the CDS Measures and Cash flow Generation Sample. It illustrates
 *  the following:
 *  
 * - Credit Curve Creation: From flat Hazard Rate, and from an array of dates and their corresponding
 * 		survival probabilities.
 * - Create Credit Curve from CDS instruments, and recover the input measure quotes.
 * - Create an SNAC CDS, price it, and display the coupon/loss cash flow.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CDSCashFlowMeasures {
	private static final java.lang.String FIELD_SEPARATOR = "   ";

	/*
	 * Sample API demonstrating the creation/usage of the credit curve from survival and hazard rates
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CreditCurveAPISample()
		throws Exception
	{
		JulianDate dtStart = DateUtil.Today();

		JulianDate dt10Y = dtStart.addYears (10);

		/*
		 * Create Credit Curve from flat Hazard Rate
		 */

		CreditCurve ccFlatHazard = ScenarioCreditCurveBuilder.FlatHazard (
			dtStart.julian(),
			"CC",
			"USD",
			0.02,
			0.4
		);

		System.out.println ("CCFromFlatHazard[" + dt10Y.toString() + "]; Survival=" +
			ccFlatHazard.survival ("10Y") + "; Hazard=" + ccFlatHazard.hazard ("10Y"));

		int[] aiDate = new int[5];
		double[] adblSurvival = new double[5];

		for (int i = 0; i < 5; ++i) {
			aiDate[i] = dtStart.addYears (2 * i + 2).julian();

			adblSurvival[i] = 1. - 0.1 * (i + 1);
		}

		/*
		 * Create Credit Curve from an array of dates and their corresponding survival probabilities
		 */

		CreditCurve ccFromSurvival = ScenarioCreditCurveBuilder.Survival (
			dtStart.julian(),
			"CC",
			"USD",
			aiDate,
			adblSurvival,
			0.4
		);

		System.out.println ("CCFromSurvival[" + dt10Y.toString() + "]; Survival=" +
			ccFromSurvival.survival ("10Y") + "; Hazard=" + ccFromSurvival.hazard ("10Y"));
	}

	/*
	 * Sample API demonstrating the creation of the Credit Curve from the CDS instruments
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static void CreateCreditCurveFromCDSInstruments()
		throws Exception
	{
		JulianDate dtStart = DateUtil.Today();

		/*
		 * Populate the instruments, the calibration measures, and the calibration quotes
		 */

		double[] adblQuotes = new double[5];
		String[] astrCalibMeasure = new String[5];
		CreditDefaultSwap[] aCDS = new CreditDefaultSwap[5];

		for (int i = 0; i < 5; ++i) {
			/*
			 * The Calibration CDS
			 */

			aCDS[i] = CDSBuilder.CreateSNAC (
				dtStart,
				(i + 1) + "Y",
				0.01,
				"CORP"
			);

			/*
			 * Calibration Quote
			 */

			adblQuotes[i] = 100.;

			/*
			 * Calibration Measure
			 */

			astrCalibMeasure[i] = "FairPremium";
		}

		/*
		 * Flat Discount Curve
		 */

		MergedDiscountForwardCurve dc = ScenarioDiscountCurveBuilder.ExponentiallyCompoundedFlatRate (
			dtStart,
			"USD",
			0.05
		);

		/*
		 * Create the Credit Curve from the give CDS instruments
		 */

		CreditCurve cc = ScenarioCreditCurveBuilder.Custom (
			"CORP",
			dtStart,
			aCDS,
			dc,
			adblQuotes,
			astrCalibMeasure,
			0.4,
			false
		);

		/*
		 * Valuation Parameters
		 */

		ValuationParams valParams = ValuationParams.Spot (
			dtStart,
			0,
			"",
			Convention.DATE_ROLL_ACTUAL
		);

		/*
		 * Standard Credit Pricer Parameters (check javadoc for details)
		 */

		CreditPricerParams pricerParams = CreditPricerParams.Standard();

		/*
		 * Re-calculate the input calibration measures for the input CDSes
		 */

		for (int i = 0; i < aCDS.length; ++i)
			System.out.println (
				"\t" + astrCalibMeasure[i] + "[" + i + "] = " +
				aCDS[i].measureValue (
					valParams, pricerParams, MarketParamsBuilder.Create (
						dc,
						null,
						null,
						cc,
						null,
						null,
						null,
						null
					),
					null,
					astrCalibMeasure[i]
				)
			);
	}

	/*
	 * Sample API demonstrating the display of the CDS coupon and loss cash flow
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void CDSAPISample()
		throws Exception
	{
		JulianDate dtStart = DateUtil.Today();

		/*
		 * Flat Discount Curve
		 */

		MergedDiscountForwardCurve dc = ScenarioDiscountCurveBuilder.ExponentiallyCompoundedFlatRate (
			dtStart,
			"USD",
			0.05
		);

		/*
		 * Flat Credit Curve
		 */

		CreditCurve cc = ScenarioCreditCurveBuilder.FlatHazard (
			dtStart.julian(),
			"CC",
			"USD",
			0.02,
			0.4
		);

		/*
		 * Component Market Parameters built from the Discount and the Credit Curves
		 */

		CurveSurfaceQuoteContainer mktParams = MarketParamsBuilder.Credit (
			dc,
			cc
		);

		/*
		 * Create an SNAC CDS
		 */

		CreditDefaultSwap cds = CDSBuilder.CreateSNAC (
			dtStart,
			"5Y",
			0.1,
			"CC"
		);

		/*
		 * Valuation Parameters
		 */

		ValuationParams valParams = ValuationParams.Spot (
			dtStart,
			0,
			"",
			Convention.DATE_ROLL_ACTUAL
		);

		/*
		 * Standard Credit Pricer Parameters (check javadoc for details)
		 */

		CreditPricerParams pricerParams = CreditPricerParams.Standard();

		System.out.println ("Loss Start     Loss End  Notl    Rec    EffDF    StartSurv  EndSurv");

		System.out.println ("----------     --------  ----    ---    -----    ---------  -------");

		/*
		 * CDS Loss Cash Flow
		 */

		for (LossQuadratureMetrics dp : cds.lossFlow (valParams, pricerParams, mktParams))
			System.out.println (
				DateUtil.YYYYMMDD (dp.startDate()) + FIELD_SEPARATOR +
				DateUtil.YYYYMMDD (dp.endDate()) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveNotional(), 1, 0, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveRecovery(), 1, 2, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.effectiveDF(), 1, 4, 1.)  + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.startSurvival(), 1, 4, 1.) + FIELD_SEPARATOR +
				FormatUtil.FormatDouble (dp.endSurvival(), 1, 4, 1.)
			);
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		EnvManager.InitEnv ("");

		CreditCurveAPISample();

		CreateCreditCurveFromCDSInstruments();

		CDSAPISample();
	}
}

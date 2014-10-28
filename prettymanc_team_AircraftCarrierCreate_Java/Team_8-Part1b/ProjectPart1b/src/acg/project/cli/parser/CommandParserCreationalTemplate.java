package acg.project.cli.parser;

import acg.project.action.ActionSet;
import acg.project.action.ActionCreationalDefine;
import acg.project.action.command.creational.define.*;
import acg.project.cli.parser.ParseException;
import acg.architecture.datatype.*;

/**
 * This class is dedicated to all Template-categorized commands.
 * 
 * @author Evan Nilson
 * 
 */
public class CommandParserCreationalTemplate implements I_Command
{
	private AngleNavigational MIN_AZIMUTH = new AngleNavigational(0);
	private Acceleration MIN_ACCELERATION = new Acceleration(0);
	private Altitude MIN_ALTITUDE = new Altitude(0);
	private Distance MIN_DISTANCE = new Distance(0);
	private AttitudePitch MIN_ELEVATION = new AttitudePitch(0);
	private Flow MIN_FLOW = new Flow(0);
	private Percent MIN_PERCENT = new Percent(0);
	private Speed MIN_SPEED = new Speed(0);
	private Weight MIN_WEIGHT = new Weight(0);
	private Time MIN_TIME = new Time(0);
	
	private Percent MAX_PERCENT = new Percent(100);
	
	protected ActionSet _actionset;

	protected String _command;

	/**
	 * Parses a command, and passes it to the appropriate method. Implemented
	 * from interface I_Command.
	 * 
	 * @throws ParseException
	 */
	public void parseCommand(ActionSet actionset, String command)
			throws ParseException
	{

		_actionset = actionset;
		_command = command;

		String[] syntax = command.split(" ");

		for (String s : syntax)
		{
			s.trim(); // clean up extra whitespaces
		}// end loop

		String firstCommand = syntax[0].toUpperCase();
		String secCommand = syntax[1].toUpperCase();

		switch (firstCommand)
		{
		case "DEFINE":
			switch (secCommand)
			{
			case "TRAP":
				defineTrap(syntax);
				break;
			case "CATAPULT":
				defineCatapult(syntax);
				break;
			case "OLS_XMT":
				defineXMT(syntax);
				break;
			case "OLS_RCV":
				defineRCV(syntax);
				break;
			case "CARRIER":
				defineCarrier(syntax);
				break;
			case "FIGHTER":
				defineFighter(syntax);
				break;
			case "TANKER":
				defineTanker(syntax);
				break;
			case "BOOM":
				String thirdCommand = syntax[2].toUpperCase();
				switch (thirdCommand)
				{
				case "MALE":
					defineBoomMale(syntax);
					break;
				case "FEMALE":
					defineBoomFemale(syntax);
					break;
				default:
					throw new ParseException("Invalid Command > " + command);
				}// end switch
			case "TAILHOOK":
				defineTailhook(syntax);
				break;
			case "BARRIER":
				defineBarrier(syntax);
				break;
			case "AUX_TANK":
				defineAuxTank(syntax);
				break;
			default:
				throw new ParseException("Invalid Command > " + command);
			}// end switch
		case "UNDEFINE":
			undefine(syntax);
			break;
		case "SHOW":
			showTemplates(syntax);
			break;
		case "LIST":
			listTemplates();
			break;
		default:
			throw new ParseException("Invalid Command > " + command);
		}

	}// end parseCommand

	/**
	 * Parses the <origin> parameters for a command and returns a
	 * CoordinateCartesianRelative object.
	 * 
	 * @return CoordinateCartesianRelative
	 */
	protected CoordinateCartesianRelative parseOrigin(String desc)
	{
		String[] origin = desc.split(":");
		origin[0] = origin[0].replaceAll("\\+", ""); 
		origin[1] = origin[1].replaceAll("\\+", "");
		int x = Integer.parseInt(origin[0]);
		int y = Integer.parseInt(origin[1]);
		return new CoordinateCartesianRelative(x, y);
	}// end parseOrigin

	/**
	 * DEFINEï TRAPï <tid>ï ORIGINï <origin>ï AZIMUTHï <azimuth>
	 * WIDTHï <distance>ï LIMITï WEIGHTï <weight> SPEEDï <speed> MISSï <percent>
	 * 
	 * Defines template tid for a trap with origin origin, azimuth azimuth,
	 * width distance, weight limit weight, speed limit speed, and miss
	 * percentage percent. This command populates CommandCreationalDefineTrap.
	 * 
	 * @throws ParseException
	 */
	protected void defineTrap(String[] desc) throws ParseException
	{
		// Make sure we have the right command
		if (desc.length != 16 || !desc[3].equalsIgnoreCase("ORIGIN")
				|| !desc[5].equalsIgnoreCase("AZIMUTH")
				|| !desc[7].equalsIgnoreCase("WIDTH")
				|| !desc[9].equalsIgnoreCase("LIMIT")
				|| !desc[10].equalsIgnoreCase("WEIGHT")
				|| !desc[12].equalsIgnoreCase("SPEED")
				|| !desc[14].equalsIgnoreCase("MISS"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		CoordinateCartesianRelative origin = parseOrigin(desc[4]);
		AngleNavigational azimuth = new AngleNavigational(
				Double.parseDouble(desc[6]));
		if (azimuth.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for azimuth > " + azimuth);
		Distance width = new Distance(Double.parseDouble(desc[8]));
		if (width.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for width > " + width);
		Weight weight = new Weight(Integer.parseInt(desc[11]));
		if (weight.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for weight > " + weight);
		Speed speed = new Speed(Integer.parseInt(desc[13]));
		if (speed.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for speed > " + speed);
		Percent miss = new Percent(Integer.parseInt(desc[15]));
		if (miss.compareTo(MIN_PERCENT) < 0 || miss.compareTo(MAX_PERCENT) > 1)
			throw new ParseException("Invalid Value for miss > " + miss);

		CommandCreationalDefineTrap defTrap = new CommandCreationalDefineTrap(
				tid, origin, azimuth, width, weight, speed, miss);
		acd.submit(defTrap);
	}// end defineTrap

	/**
	 * DEFINEï CATAPULTï <tid>ï ORIGINï <origin>ï AZIMUTH <azimuth>ï LENGTHï <distance>
	 * ACCELERATION <acceleration>
	 * LIMITï WEIGHTï <weight>ï SPEEDï <speed>ï RESETï <time>
	 * 
	 * Defines template tid for a catapult with origin origin, azimuth azimuth,
	 * length distance, acceleration acceleration, weight limit weight terminal
	 * speed speed and reset time time. This command populates
	 * CommandCreationalDefineCatapult.
	 * 
	 * @throws ParseException
	 */
	protected void defineCatapult(String[] desc) throws ParseException
		{
		if(desc.length != 18 || !desc[3].equalsIgnoreCase("ORIGIN") 
				|| !desc[5].equalsIgnoreCase("AZIMUTH") 
				|| !desc[7].equalsIgnoreCase("LENGTH") 
				|| !desc[9].equalsIgnoreCase("ACCELERATION")
				|| !desc[11].equalsIgnoreCase("LIMIT") 
				|| !desc[12].equalsIgnoreCase("WEIGHT") 
				|| !desc[14].equalsIgnoreCase("SPEED") 
				|| !desc[16].equalsIgnoreCase("RESET"))
					throw new ParseException("Invalid Command > " + getString(desc));
		
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		
		Identifier tid = new Identifier(desc[2]);
		CoordinateCartesianRelative origin = parseOrigin(desc[4]);
		AngleNavigational azimuth = new AngleNavigational(Double.parseDouble(desc[6]));
		if(azimuth.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for azimuth > " + azimuth);
		Distance length = new Distance(Double.parseDouble(desc[8]));
		if(length.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for length > " + length);
		Acceleration acc = new Acceleration(Double.parseDouble(desc[10]));
		if(acc.compareTo(MIN_ACCELERATION) < 0)
			throw new ParseException("Invalid Value for acceleration > " + acc);
		Weight limWeight = new Weight(Integer.parseInt(desc[13]));
		if(limWeight.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for limit weight > " + limWeight);
		Speed speed = new Speed(Integer.parseInt(desc[15]));
		if(speed.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for speed > " + speed);
		Time reset = new Time(Double.parseDouble(desc[17]));
		if(reset.compareTo(MIN_TIME) < 0)
			throw new ParseException("Invalid Value for reset > " + reset);
		
		CommandCreationalDefineCatapult defCat = new CommandCreationalDefineCatapult(tid, origin, azimuth, length, acc, limWeight, speed, reset);
		acd.submit(defCat);
		}//end defineCatapult

	/**
	 * DEFINEï OLS_XMTï <tid>ï ORIGINï <origin>ï AZIMUTH
	 * <azimuth>ï ELEVATIONï <elevation>ï RANGEï <distance1> DIAMETERï <distance2>
	 * 
	 * Defines template tid for an optical-landing-system transmitter with
	 * origin origin, azimuth azimuth, approach elevation elevation, approach
	 * range distance1, and approach tunnel diameter distance2. This command
	 * populates CommandCreationalDefineOLSTransmitter.
	 * 
	 * @throws ParseException
	 */
	protected void defineXMT(String[] desc) throws ParseException
	{
		if (desc.length != 13 || !desc[3].equalsIgnoreCase("ORIGIN")
				|| !desc[5].equalsIgnoreCase("AZIMUTH")
				|| !desc[7].equalsIgnoreCase("ELEVATION")
				|| !desc[9].equalsIgnoreCase("RANGE")
				|| !desc[11].equalsIgnoreCase("DIAMETER"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		CoordinateCartesianRelative origin = parseOrigin(desc[4]);
		AngleNavigational azimuth = new AngleNavigational(
				Double.parseDouble(desc[6]));
		if (azimuth.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for azimuth > " + azimuth);
		AttitudePitch elevation = new AttitudePitch(Double.parseDouble(desc[8]));
		if (elevation.compareTo(MIN_ELEVATION) < 0)
			throw new ParseException("Invalid Value for elevation > "
					+ elevation);
		Distance range = new Distance(Double.parseDouble(desc[10]));
		if (range.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for range > " + range);
		Distance diameter = new Distance(Double.parseDouble(desc[12]));
		if (diameter.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for diameter > " + diameter);
		CommandCreationalDefineOLSTransmitter defTrans = new CommandCreationalDefineOLSTransmitter(
				tid, origin, azimuth, elevation, range, diameter);
		acd.submit(defTrans);
	}// end defineXMT

	/**
	 * DEFINEï OLS_RCVï <tid>ï DIAMETERï <distance> Defines template tid for an
	 * optical-landing-system receiver with approach tunnel diameter distance.
	 * This command populates CommandCreationalDefineOLSReceiver.
	 * 
	 * @throws ParseException
	 */
	protected void defineRCV(String[] desc) throws ParseException
	{
		if (desc.length != 5 || !desc[3].equalsIgnoreCase("DIAMETER"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		Distance distance = new Distance(Double.parseDouble(desc[4]));
		if (distance.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for diameter > " + distance);

		CommandCreationalDefineOLSReceiver defRec = new CommandCreationalDefineOLSReceiver(
				tid, distance);
		acd.submit(defRec);
	}// end defineRCV

	/**
	 * DEFINEï CARRIERï <tid>ï SPEEDï MAXï <speed1>ï DELTA INCREASEï 
	 * <speed2>ï DECREASEï <speed3>ï TURNï <azimuth> LAYOUTï <string>
	 * 
	 * Defines template tid for a carrier with maximum speed speed1, delta speed
	 * increasing speed2, delta speed decreasing speed3, delta turn angle
	 * azimuth, and layout filename string. This command populates
	 * CommandCreationalDefineCarrier.
	 * 
	 * @throws ParseException
	 */
	protected void defineCarrier(String[] desc) throws ParseException
		{
		if(desc.length != 15 || !desc[3].equalsIgnoreCase("SPEED") || !desc[4].equalsIgnoreCase("MAX") || !desc[6].equalsIgnoreCase("DELTA") || !desc[7].equalsIgnoreCase("INCREASE")
				|| !desc[9].equalsIgnoreCase("DECREASE") || !desc[11].equalsIgnoreCase("TURN") || !desc[13].equalsIgnoreCase("LAYOUT"))
			throw new ParseException("Invalid Command > " + getString(desc));
		
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		
		Identifier tid = new Identifier(desc[2]);
		Speed speedMax = new Speed(Integer.parseInt(desc[5]));
		if(speedMax.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for max speed > " + speedMax);
		Speed deltaIncrease = new Speed(Integer.parseInt(desc[8]));
		if(deltaIncrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for speed delta increase > " + deltaIncrease);
		Speed deltaDecrease = new Speed(Integer.parseInt(desc[10]));
		if(deltaDecrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for speed delta decrease > " + deltaDecrease);
		AngleNavigational turn = new AngleNavigational(Double.parseDouble(desc[12]));
		if(turn.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for turn > " + turn);
		String layout = desc[14];
		
		CommandCreationalDefineCarrier defCar = new CommandCreationalDefineCarrier(tid, speedMax, deltaIncrease, deltaIncrease, turn, layout);
		acd.submit(defCar);
		}//end defineCarrier

	/**
	 * DEFINEï FIGHTERï <tid>ï SPEEDï MINï speedmin<speed1>
	 * MAXï speedmax<speed2>ï DELTAï INCREASE dspeedinc<speed3>
	 * DECREASEï dspeeddec<speed4>ï TURNï dturn<azimuth> CLIMB dclimb<altitude1>
	 * DESCENTï ddescent<altitude2>ï EMPTYï WEIGHTï 
	 * weight<weight1>ï FUELï INITIALï fuelinit<weight2>ï  DELTAï dfuel<weight3>
	 * 
	 * Defines template tid for a fighter with minimum speed speed1, maximum
	 * speed speed2, delta increasing speed speed3, delta decreasing speed
	 * speed4, delta turn angle azimuth, delta climbing speed altitude1 feet per
	 * minute (FPM), delta descending speed altitude2 FPM, empty aircraft weight
	 * weight1, fuel-tank quantity weight2, fuel burn rate weight3 per knot of
	 * speed. This command populates CommandCreationalDefineFighter.
	 * 
	 * @throws ParseException
	 */
	protected void defineFighter(String[] desc) throws ParseException
	{
		if (desc.length != 27 || !desc[3].equalsIgnoreCase("SPEED")
				|| !desc[4].equalsIgnoreCase("MIN")
				|| !desc[6].equalsIgnoreCase("MAX")
				|| !desc[8].equalsIgnoreCase("DELTA")
				|| !desc[9].equalsIgnoreCase("INCREASE")
				|| !desc[11].equalsIgnoreCase("DECREASE")
				|| !desc[13].equalsIgnoreCase("TURN")
				|| !desc[15].equalsIgnoreCase("CLIMB")
				|| !desc[17].equalsIgnoreCase("DESCENT")
				|| !desc[19].equalsIgnoreCase("EMPTY")
				|| !desc[20].equalsIgnoreCase("WEIGHT")
				|| !desc[22].equalsIgnoreCase("FUEL")
				|| !desc[23].equalsIgnoreCase("INITIAL")
				|| !desc[25].equalsIgnoreCase("DELTA"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		Speed speedMin = new Speed(Integer.parseInt(desc[5]));
		if (speedMin.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for min speed > "
					+ speedMin);
		Speed speedMax = new Speed(Integer.parseInt(desc[7]));
		if (speedMax.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for max speed > "
					+ speedMax);
		Speed dIncrease = new Speed(Integer.parseInt(desc[10]));
		if (dIncrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException(
					"Invalid Value for speed delta increase > " + dIncrease);
		Speed dDecrease = new Speed(Integer.parseInt(desc[12]));
		if (dDecrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException(
					"Invalid Value for speed delta decrease > " + dDecrease);
		AngleNavigational turn = new AngleNavigational(
				Double.parseDouble(desc[14]));
		if (turn.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for turn > " + turn);
		Altitude climb = new Altitude(Double.parseDouble(desc[16]));
		if (climb.compareTo(MIN_ALTITUDE) < 0)
			throw new ParseException("Invalid Value for climb > " + climb);
		Altitude descent = new Altitude(Double.parseDouble(desc[18]));
		if (descent.compareTo(MIN_ALTITUDE) < 0)
			throw new ParseException("Invalid Value for descent > " + descent);
		Weight empWeight = new Weight(Integer.parseInt(desc[21]));
		if (empWeight.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for empty weight > "
					+ empWeight);
		Weight fuelInit = new Weight(Integer.parseInt(desc[24]));
		if (fuelInit.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for fuel initial > "
					+ fuelInit);
		Weight dFuel = new Weight(Integer.parseInt(desc[26]));
		if (dFuel.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for fuel delta > " + dFuel);

		CommandCreationalDefineFighter defFighter = new CommandCreationalDefineFighter(
				tid, speedMin, speedMax, dIncrease, dDecrease, turn, climb,
				descent, empWeight, fuelInit, dFuel);
		acd.submit(defFighter);
	}// end defineFighter

	/**
	 * DEFINEï TANKERï <tid>ï SPEEDï MINï <speed1>ï MAXï <speed2>
	 * DELTAï INCREASEï <speed3>ï DECREASEï <speed4>
	 * TURNï <azimuth>ï CLIMBï <altitude1>ï DESCENT <altitude2>ï TANKï <weight>
	 * 
	 * Defines template tid for a tanker with minimum speed speed1, maximum
	 * speed speed2, delta increasing speed speed3, delta decreasing speed
	 * speed4, delta turn angle azimuth, delta climbing speed altitude1 FPM,
	 * delta descending speed altitude2 FPM, and fuel-tank quantity weight. This
	 * command populates CommandCreationalDefineTanker.
	 * 
	 * @throws ParseException
	 */
	protected void defineTanker(String[] desc) throws ParseException
	{
		if (desc.length != 21 || !desc[3].equalsIgnoreCase("SPEED")
				|| !desc[4].equalsIgnoreCase("MIN")
				|| !desc[6].equalsIgnoreCase("MAX")
				|| !desc[8].equalsIgnoreCase("DELTA")
				|| !desc[9].equalsIgnoreCase("INCREASE")
				|| !desc[11].equalsIgnoreCase("DECREASE")
				|| !desc[13].equalsIgnoreCase("TURN")
				|| !desc[15].equalsIgnoreCase("CLIMB")
				|| !desc[17].equalsIgnoreCase("DESCENT")
				|| !desc[19].equalsIgnoreCase("TANK"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		Speed speedMin = new Speed(Integer.parseInt(desc[5]));
		if (speedMin.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for min speed > "
					+ speedMin);
		Speed speedMax = new Speed(Integer.parseInt(desc[7]));
		if (speedMax.compareTo(MIN_SPEED) < 0)
			throw new ParseException("Invalid Value for max speed > "
					+ speedMax);
		Speed speedIncrease = new Speed(Integer.parseInt(desc[10]));
		if (speedIncrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException(
					"Invalid Value for speed delta increase > " + speedIncrease);
		Speed speedDecrease = new Speed(Integer.parseInt(desc[12]));
		if (speedDecrease.compareTo(MIN_SPEED) < 0)
			throw new ParseException(
					"Invalid Value for speed delta decrease > " + speedDecrease);
		AngleNavigational turn = new AngleNavigational(
				Double.parseDouble(desc[14]));
		if (turn.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for turn > " + turn);
		Altitude climb = new Altitude(Double.parseDouble(desc[16]));
		if (climb.compareTo(MIN_ALTITUDE) < 0)
			throw new ParseException("Invalid Value for climb > " + climb);
		Altitude descent = new Altitude(Double.parseDouble(desc[18]));
		if (descent.compareTo(MIN_ALTITUDE) < 0)
			throw new ParseException("Invalid Value for descent > " + descent);
		Weight weight = new Weight(Integer.parseInt(desc[20]));
		if (weight.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for tank weight > "
					+ weight);
		CommandCreationalDefineTanker defTanker = new CommandCreationalDefineTanker(
				tid, speedMin, speedMax, speedIncrease, speedDecrease, turn,
				climb, descent, weight);
		acd.submit(defTanker);
	}// end defineTanker

	/**
	 * DEFINEï BOOMï MALEï <tid>ï LENGTHï <distance1>
	 * DIAMETERï <distance2>ï FLOWï <weight>
	 * 
	 * Defines template tid for a male boom with length distance1, capture
	 * diameter distance2, and fuel flow weight in pounds per second. This
	 * command populates CommandCreationalDefineBoomMale.
	 * 
	 * @throws ParseException
	 */
	protected void defineBoomMale(String[] desc) throws ParseException
		{
		if(desc.length !=  10 || !desc[4].equalsIgnoreCase("LENGTH") || !desc[6].equalsIgnoreCase("DIAMETER") || !desc[8].equalsIgnoreCase("FLOW"))
			throw new ParseException("Invalid Command > " + getString(desc));
		
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		
		Identifier tid = new Identifier(desc[3]);
		Distance length = new Distance(Double.parseDouble(desc[5]));
		if(length.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for length > " + length);
		Distance diameter = new Distance(Double.parseDouble(desc[7]));
		if(diameter.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for diameter > " + diameter);
		Flow flow = new Flow(Double.parseDouble(desc[9]));
		if(flow.compareTo(MIN_FLOW) < 0)
			throw new ParseException("Invalid Value for flow > " + flow);
		
		CommandCreationalDefineBoomMale defBoomMale = new CommandCreationalDefineBoomMale(tid, length, diameter, flow);
		acd.submit(defBoomMale);
		}//end defineBoomMale

	/**
	 * DEFINEï BOOMï FEMALEï <tid>ï LENGTHï <distance1>ï DIAMETER
	 * <distance2>ï ELEVATIONï <elevation> FLOWï <weight>
	 * 
	 * Defines template tid for a female boom with length distance1, capture
	 * diameter distance2, downward elevation elevation, and fuel flow weight in
	 * pounds per second. This command populates
	 * CommandCreationalDefineBoomFemale.
	 * 
	 * @throws ParseException
	 */
	protected void defineBoomFemale(String[] desc) throws ParseException
	{
		if (desc.length != 12 || !desc[4].equalsIgnoreCase("LENGTH")
				|| !desc[6].equalsIgnoreCase("DIAMETER")
				|| !desc[8].equalsIgnoreCase("ELEVATION")
				|| !desc[10].equalsIgnoreCase("FLOW"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[3]);
		Distance length = new Distance(Double.parseDouble(desc[5]));
		if (length.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for length > " + length);
		Distance diameter = new Distance(Double.parseDouble(desc[7]));
		if (diameter.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for diameter > " + diameter);
		AttitudePitch elevation = new AttitudePitch(Double.parseDouble(desc[9]));
		if (elevation.compareTo(MIN_ELEVATION) < 0)
			throw new ParseException("Invalid Value for elevation > "
					+ elevation);
		Flow flow = new Flow(Double.parseDouble(desc[11]));
		if (flow.compareTo(MIN_FLOW) < 0)
			throw new ParseException("Invalid Value for flow > " + flow);
		CommandCreationalDefineBoomFemale defBoomFemale = new CommandCreationalDefineBoomFemale(tid, length, diameter, elevation, flow);
		acd.submit(defBoomFemale);
	}// end defineBoomFemale

	/**
	 * DEFINEï TAILHOOKï <tid>ï TIMEï <time>
	 * 
	 * Defines template tid for a tailhook with extend/retract time time. This
	 * command populates CommandCreationalDefineTailhook.
	 * 
	 * @throws ParseException
	 */
	protected void defineTailhook(String[] desc) throws ParseException
	{
		if (desc.length != 5 || !desc[3].equalsIgnoreCase("TIME"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		Time time = new Time(Double.parseDouble(desc[4]));
		if (time.compareTo(MIN_TIME) < 0)
			throw new ParseException("Invalid Value for time > " + time);

		CommandCreationalDefineTailhook defTailhook = new CommandCreationalDefineTailhook(
				tid, time);
		acd.submit(defTailhook);
	}// end defineTailhook

	/**
	 * DEFINEï BARRIERï <tid>ï ORIGINï <origin>ï AZIMUTHï <azimuth>ï WIDTHï <distance>ï 
	 * TIMEï <time>
	 * 
	 * Defines template tid for a blast barrier with width distance feet
	 * centered at origin and angled at azimuth degrees with raise/lower time
	 * time. This command populates CommandCreationalDefineBarrier.
	 * 
	 * @throws ParseException
	 */
	protected void defineBarrier(String[] desc) throws ParseException
		{
		if(desc.length != 11 || !desc[3].equalsIgnoreCase("ORIGIN") || !desc[5].equalsIgnoreCase("AZIMUTH") || !desc[7].equalsIgnoreCase("WIDTH") || !desc[9].equalsIgnoreCase("TIME"))
			throw new ParseException("Invalid Command > " + getString(desc));
		
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		
		Identifier tid = new Identifier(desc[2]);
		CoordinateCartesianRelative origin = parseOrigin(desc[3]);
		AngleNavigational azimuth = new AngleNavigational(Double.parseDouble(desc[6]));
		if(azimuth.compareTo(MIN_AZIMUTH) < 0)
			throw new ParseException("Invalid Value for azimuth > " + azimuth);
		Distance width = new Distance(Double.parseDouble(desc[8]));
		if(width.compareTo(MIN_DISTANCE) < 0)
			throw new ParseException("Invalid Value for width > " + width);
		Time time = new Time(Double.parseDouble(desc[10]));
		if(time.compareTo(MIN_TIME) < 0)
			throw new ParseException("Invalid Value for time > " + time);
		CommandCreationalDefineBarrier defBarrier = new CommandCreationalDefineBarrier(tid, origin, azimuth, width, time);
		acd.submit(defBarrier);
		}//end defineBarrier

	/**
	 * DEFINEï AUX_TANKï <tid>ï AMOUNTï <weight>
	 * 
	 * Defines template tid for an auxiliary fuel tank with weight pounds of
	 * fuel. This command populates CommandCreationalDefineAuxiliaryTank.
	 * 
	 * @throws ParseException
	 */
	protected void defineAuxTank(String[] desc) throws ParseException
	{
		if (desc.length != 5 || !desc[3].equalsIgnoreCase("AMOUNT"))
			throw new ParseException("Invalid Command > " + getString(desc));

		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();

		Identifier tid = new Identifier(desc[2]);
		Weight amount = new Weight(Integer.parseInt(desc[4]));
		if (amount.compareTo(MIN_WEIGHT) < 0)
			throw new ParseException("Invalid Value for amount > " + amount);

		CommandCreationalDefineAuxiliaryTank defAuxTank = new CommandCreationalDefineAuxiliaryTank(
				tid, amount);
		acd.submit(defAuxTank);
	}// end defineAuxTank

	/**
	 * UNDEFINEï <tid>
	 * 
	 * Undefines template tid. This command populates CommandCreationalUndefine.
	 * 
	 */
	protected void undefine(String[] desc)
	{
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		Identifier tid = new Identifier(desc[1]);
		CommandCreationalUndefine undefine = new CommandCreationalUndefine(tid);
		acd.submit(undefine);
	}// end undefine

	/**
	 * SHOWï TEMPLATEï <tid>
	 * 
	 * Outputs the definition of template tid. This command populates
	 * CommandCreationalShowTemplate.
	 * 
	 */
	protected void showTemplates(String[] desc)
	{
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		Identifier tid = new Identifier(desc[2]);
		CommandCreationalShowTemplate showTemplate = new CommandCreationalShowTemplate(tid);
		acd.submit(showTemplate);
	}// end showTemplates

	/**
	 * LISTï TEMPLATES
	 * 
	 * Outputs the identifiers of all templates. This command populates
	 * CommandCreationalListTemplates.
	 * 
	 */
	protected void listTemplates()
		{
		ActionCreationalDefine acd = _actionset.getActionCreationalDefine();
		
		acd.submit(new CommandCreationalListTemplates());
		}//end listTemplates
	
	public String getString(String[] desc)
	{
		String s = "";
		for(String word : desc)
		{
			s += (word + " ");
		}
		return s;
	}

}// end class
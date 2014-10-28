package acg.project.cli.parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import acg.project.action.ActionSet;
import acg.project.action.ActionCreationalCreate;
import acg.project.action.command.ParameterAssignment;
import acg.project.action.command.creational.create.*;
import acg.project.action.command.creational.define.*;
import acg.architecture.datatype.Altitude;
import acg.architecture.datatype.AngleNavigational;
import acg.architecture.datatype.CoordinateWorld;
import acg.architecture.datatype.Identifier;
import acg.architecture.datatype.Latitude;
import acg.architecture.datatype.Longitude;
import acg.architecture.datatype.Speed;
import acg.project.cli.parser.ParseException;
import acg.project.map.MapTemplate;

//==================================================================================================================================================================================
/**
 * Actor agents are the agents in the simulation that contain secondary agents.
 * The contents of these commands populate a command object in
 * acg.project.action.command.creational.create and submit it to
 * ActionSet::getActionCreationalCreate().submit() from your parser.
 * 
 * This class handles command identifiers - CREATE, UNCREATE, DESCRIBE, LIST
 * AGENTS
 * 
 * @author Colton Prettyman
 * 
 */

public class CommandParserCreationalAgents implements I_Command
{

	protected ActionSet _actionSet;
	protected String _command;

	// Note: Implicit empty constructor here.

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * This override method takes an Actionset and String command as its
	 * parameter and switches on input to parse the command and create the
	 * correct agent.
	 * 
	 * @param actionset
	 *            - The actionset
	 * @param command
	 *            - The input string containing the command information.
	 * @throws ParseException
	 */
	public void parseCommand(ActionSet actionset, String command)
			throws ParseException
	{
		_actionSet = actionset;
		_command = command;
		ActionCreationalCreate acc = actionset.getActionCreationalCreate();

		String[] pcommand = parseString(command);

		if (pcommand[0].equalsIgnoreCase("CREATE"))
		{

			switch (pcommand[1].toUpperCase())
			{

			case "CARRIER":
				CreateCarrier(command, pcommand, acc);
				break;
			case "FIGHTER":
				CreateFighter(command, pcommand, acc);
				break;
			case "TANKER":
				CreateTanker(command, pcommand, acc);
				break;
			case "TRAP":
				CreateTrap(command, pcommand, acc);
				break;
			case "BARRIER":
				CreateBarrier(command, pcommand, acc);
				break;
			case "AUX_TANK":
				CreateAux_Tank(command, pcommand, acc);
				break;
			case "CATAPULT":
				CreateCatapult(command, pcommand, acc);
				break;
			case "OLS_XMT":
				CreateOls_XMT(command, pcommand, acc);
				break;
			case "OLS_RCV":
				CreateOls_RCV(command, pcommand, acc);
				break;
			case "BOOM":
				CreateBoom(command, pcommand, acc, actionset);
				break;
			case "TAILHOOK":
				CreateTailHook(command, pcommand, acc);
				break;
			default:
				throw new ParseException("Invalid command > " + command);

			} // end switch
		}// end if
		else
		{
			switch (pcommand[0].toUpperCase())
			{

			case "UNCREATE":
				Uncreate(command, pcommand, acc);
				break;

			case "DESCRIBE":
				Describe(command, pcommand, acc);
				break;

			case "LIST":
				ListAgents(command, pcommand, acc);
				break;

			default:
				throw new ParseException("Invalid command > " + command);
			}
		} // end else

	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 *     0    1        2      3   4     5       6     7       8     9      10    11   12    13
	 * CREATE CARRIER <aid1> FROM <tid> WITH CATAPULT <aid2> BARRIER <aid3> TRAP <aid4> OLS <aid5> 
	 * 14     15          16           17      18      19    20
	 * AT COORDINATES <coordinates> HEADING <course> SPEED <speed> Creates
	 * carrier aid1 from carrier template tid with catapult aid2, barrier aid3,
	 * trap aid4, optical-landing-system transmitter aid5 at coordinates
	 * coordinates with heading course and speed speed. This command populates
	 * CommandCreationalCreateCarrier.
	 * 
	 * @throws ParseException
	 */
	protected void CreateCarrier(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		// I tested this regex to work and it does...thank goodness
		String regex = "CREATE\\s+CARRIER\\s+\\w+\\s+FROM\\s+\\w+\\s+WITH\\s+CATAPULT\\s+\\w+\\s+BARRIER\\s+\\w+\\s+TRAP\\s+\\w+\\s+OLS\\s+\\w+\\s+AT\\s+"
				+ "COORDINATES\\s+\\d*\\*\\d*'\\d*\\.?\\d*\"/\\d*\\*\\d*'\\d*\\.?\\d*\"\\s+HEADING\\s+\\d{3}\\s+SPEED\\s+\\d+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		if (!p.matcher(input).matches())
			throw new ParseException("Invalid Input > " + input);

		Identifier carrieraid = new Identifier(command[2]);
		Identifier carriertid = new Identifier(command[4]);
		Identifier catapultaid = new Identifier(command[7]);
		Identifier barrieraid = new Identifier(command[9]);
		Identifier trapaid = new Identifier(command[11]);
		Identifier olsaid = new Identifier(command[13]);
		CoordinateWorld coordinates = parseCoordinates(command[16]);

		AngleNavigational course = new AngleNavigational(Double.parseDouble(command[18]));

		int tempspeed;
		tempspeed = Integer.parseInt(command[20]);
		if (tempspeed < 0)
			throw new ParseException("Invalid speed" + command[20]);
		Speed speed = new Speed(tempspeed);

		CommandCreationalCreateCarrier cccc = new CommandCreationalCreateCarrier(
				carrieraid, carriertid, catapultaid, barrieraid, trapaid,
				olsaid, coordinates, course, speed);

		acc.submit(cccc);
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 *    0     1       2      3    4     5   6     7     8     9      10      11     
	 * CREATE FIGHTER <aid1> FROM <tid> WITH OLS <aid2> BOOM <aid3> TAILHOOK <aid4> [TANKS <aidn>+] 
	 *  [OVERRIDING (<aidm>.<argname> WITH <string>)+] [AT COORDINATES
	 * <coordinates> ALTITUDE <altitude>  HEADING <course> SPEED
	 * <speed>] Creates fighter aid1 from fighter template tid,
	 * optical-landing-system receiver aid2, female boom aid3, tailhook aid4,
	 * and optional auxiliary fuel tanks aidn. The optional initial airborne
	 * state dictates that the fighter must start in the air at coordinates
	 * coordinates and altitude altitude with heading course and speed speed. A
	 * fighter aloft may not be used in the POPULATE CARRIER command. The value
	 * of any named argument argname in aidm can be overridden with string,
	 * where it is in the appropriate format. This command populates
	 * CommandCreationalCreateFighter.
	 * 
	 * @throws ParseException
	 */
	protected void CreateFighter(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		// This regex has been tested!!
		String regex = "\\s*CREATE\\s+FIGHTER\\s+\\w+\\s+FROM\\s+\\w+\\s+WITH\\s+OLS\\s+\\w+\\s+BOOM\\s+\\w+\\s+TAILHOOK\\s+\\w+\\s*"
				+ "\\s*(TANKS(\\s+\\w+\\s*){1,})*\\s*(OVERRIDING(\\s+\\w+\\.\\w+\\s+WITH\\s+\\w+\\s*){1,})*"
				+ "\\s*(AT\\s+COORDINATES\\s+\\d*\\*\\d*'\\d*\\.?\\d*\"/\\d*\\*\\d*'\\d*\\.?\\d*\"\\s+ALTITUDE\\s+\\d+\\s+HEADING\\s+\\d{3}\\s+SPEED\\s+\\d+)*\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);

		if (!m.matches())
			throw new ParseException("Invalid command > " + input);

		Identifier fighteraid = new Identifier(command[2]);
		Identifier fightertid = new Identifier(command[4]);
		Identifier olsaid = new Identifier(command[7]);
		Identifier boomaid = new Identifier(command[9]);
		Identifier tailhookaid = new Identifier(command[11]);
		List<Identifier> tankaids = parseTankAids(input);
		List<ParameterAssignment> parameters = parseFighterParameters(input);

		CommandCreationalCreateFighter cccf = null;
		regex = "AT\\s+COORDINATES\\s+\\d*\\*\\d*'\\d*\\.?\\d*\"/\\d*\\*\\d*'\\d*\\.?\\d*\"\\s+ALTITUDE\\s+\\d+\\s+HEADING\\s+\\d{3}\\s+SPEED\\s+\\d+\\s*";
		p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		m = p.matcher(input);

		// Check to see if the command contains a set of coordinates, altitude,
		// and heading
		if (m.find())
		{
			// Deal with coordinates
			regex = "\\d*\\*\\d*'\\d*\\.?\\d*\"/\\d*\\*\\d*'\\d*\\.?\\d*\"";
			m = Pattern.compile(regex).matcher(input);
			m.find();
			CoordinateWorld coordinates = parseCoordinates(m.group());

			// Deal with the altutude
			regex = "ALTITUDE\\s+\\d+";
			m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input);
			m.find();
			String[] tempSplit = parseString(m.group());
			int height = Integer.parseInt(tempSplit[1]);
			if (height < 0)
				throw new ParseException("Invalid altitude > " + tempSplit[1]);
			Altitude altitude = new Altitude(height);

			// Deal with the heading
			regex = "HEADING\\s+\\d{3}";
			m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input);
			m.find();
			tempSplit = parseString(m.group());
			int angle = Integer.parseInt(tempSplit[1]);
			if (angle < 0 || angle > 359.99)
				throw new ParseException("Invalid heading > " + tempSplit[1]);
			AngleNavigational heading = new AngleNavigational(angle);

			// Now deal with speed
			regex = "SPEED\\s+\\d+";
			m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input);
			m.find();
			tempSplit = parseString(m.group());
			int speedint = Integer.parseInt(tempSplit[1]);
			if (speedint < 0)
				throw new ParseException("Invalid heading > " + tempSplit[1]);
			Speed speed = new Speed(speedint);

			// allocate the commandcreationalcreatefighter with the coordinates,
			// altitude, heading, and speed
			cccf = new CommandCreationalCreateFighter(fighteraid, fightertid,
					olsaid, boomaid, tailhookaid, tankaids, parameters,
					coordinates, altitude, heading, speed);
		} else
			// allocate the commandcreationalcreatefighter. No coordinates...
			cccf = new CommandCreationalCreateFighter(fighteraid, fightertid,
					olsaid, boomaid, tailhookaid, tankaids, parameters);

		acc.submit(cccf);
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * * 0 1 2 3 4 5 6 7 8 9 10 11 12 13 CREATE FIGHTER <aid1> FROM <tid> WITH
	 * OLS <aid2> BOOM <aid3> TAILHOOK <aid4> [TANKS <aidn>+] 14 15 16 17 18 19
	 * 20 21 22 23 [OVERRIDING (<aidm>.<argname> WITH <string>)+] [AT
	 * COORDINATES <coordinates> ALTITUDE <altitude> 24 25 26 27 HEADING
	 * <course> SPEED <speed>] This method finds any possible parameters in the
	 * command, parses them out and adds them to a list of ParameterAssignment
	 * type
	 * 
	 * @param command
	 * @return the list of parameters
	 */
	private List<ParameterAssignment> parseFighterParameters(String command)
	{
		List<ParameterAssignment> paramList = new LinkedList<ParameterAssignment>();
		String regex = "OVERRIDING(\\s+\\w+\\.\\w+\\s+WITH\\s+\\w+\\s*)+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(command);

		// Check to see if there are parameters even.
		if (!m.find())
			return paramList; // No parameters...empty list!

		// Grab the parameters from the command
		String paramString = m.group();

		// for all sub-parameters in this paramString
		regex = "\\w+\\.\\w+\\s+WITH\\s+\\w+";
		p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		m = p.matcher(paramString);

		String[] subParamSplit;
		// While there are parameters still in the paramString keep pulling them
		// out
		// and allocating accordingly
		while (m.find())
		{
			// 0 1 2
			// Should pull a string like this. "<aidm>.<argname> WITH <string>"
			subParamSplit = parseString(m.group());
			Identifier aidm = new Identifier(subParamSplit[0]);

			paramList.add(new ParameterAssignment(aidm, subParamSplit[2]));
		}

		return paramList;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * * * 0 1 2 3 4 5 6 7 8 9 10 11 12 13 CREATE FIGHTER <aid1> FROM <tid> WITH
	 * OLS <aid2> BOOM <aid3> TAILHOOK <aid4> [TANKS <aidn>+] 14 15 16 17 18 19
	 * 20 21 22 23 [OVERRIDING (<aidm>.<argname> WITH <string>)+] [AT
	 * COORDINATES <coordinates> ALTITUDE <altitude> 24 25 26 27 HEADING
	 * <course> SPEED <speed>] This method finds any possible tank aids in the
	 * command, parses them out stores them in a List of Identifiers.
	 * 
	 * @param command
	 * @return the list of tank aids
	 */
	private List<Identifier> parseTankAids(String command)
	{
		List<Identifier> tankaidList = new LinkedList<Identifier>();

		String regex = "TANKS(\\s+\\w+\\s*){1,}";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(command);

		// No tank aids..return early
		if (!m.find())
			return tankaidList;

		String tankaidString = m.group();
		tankaidString = tankaidString.trim();
		regex = "\\s+\\w\\s*";
		p = Pattern.compile(regex);
		m = p.matcher(tankaidString);

		String[] tankaidSplit;
		// While there are tank aids keep grabbing them
		while (m.find())
		{
			// Split the substring
			tankaidSplit = parseString(m.group());
			// add the tank aid to the list
			tankaidList.add(new Identifier(tankaidSplit[0]));
		}

		return tankaidList;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 *   0       1      2     3    4     5    6     7    8     9             10         11        12 
	 * CREATE TANKER <aid1> FROM <tid> WITH BOOM <aid2> AT COORDINATES <coordinates> ALTITUDE <altitude> 
	 *     13     14     15      16
	 * HEADING <course> SPEED <speed> Creates tanker aid1 from tanker template
	 * tid and mail boom aid2 in the air at coordinates coordinates and altitude
	 * altitude with heading course and speed speed. This command populates
	 * CommandCreationalCreateTanker.
	 * 
	 * @throws ParseException
	 */
	protected void CreateTanker(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+TANKER\\s+\\w+\\s+FROM\\s+\\w+\\s+WITH\\s+BOOM\\s+\\w+\\s+AT\\s+"
				+ "COORDINATES\\s+\\d*\\*\\d*'\\d*\\.?\\d*\"/\\d*\\*\\d*'\\d*\\.?\\d*\"\\s+ALTITUDE\\s+\\d+\\s+HEADING\\s+\\d{3}\\s+SPEED\\s+\\d+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		int temp;

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);
		Identifier aid2 = new Identifier(command[7]);
		CoordinateWorld coordinates = parseCoordinates(command[10]);

		temp = Integer.parseInt(command[12]);

		if (temp < 0)
			throw new ParseException("Invalid Altitude > " + input);
		Altitude altitude = new Altitude(temp);

		int heading = Integer.parseInt(command[14]);
		if (heading < 0 || heading > 359.99)
			throw new ParseException("Invalid heading > " + command[14]);
		AngleNavigational course = new AngleNavigational(heading);

		temp = Integer.parseInt(command[16]);
		if (temp < 0)
			throw new ParseException("Invalid speed" + command[16]);
		Speed speed = new Speed(temp);

		acc.submit(new CommandCreationalCreateTanker(aid, tid, aid2,
				coordinates, altitude, course, speed));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Helper method for coordinate parsing in createTanker
	 * 
	 * coordinates latitude/longitude 45*30'15�/110*30'10� CoordinateWorld
	 * degrees and minutes are integers on [0,90] and [0,60), respectively;
	 * seconds is a double on [0,60).
	 * 
	 * @param command
	 * @return a coordinateworld object parsed from the input
	 * @throws ParseException
	 */
	private CoordinateWorld parseCoordinates(String command)
			throws ParseException
	{
		command = command.replace("/", "");
		command = command.trim();
		String[] pcommand = command.split("\\*|\"|'");

		String regex = "\\d*\\*\\d*'\\d*\\.?\\d*\"\\d*\\*\\d*'\\d*\\.?\\d*\"";
		Pattern p = Pattern.compile(regex);
		if (!p.matcher(command).matches())
			throw new ParseException("Invalid Coordinates > " + command);

		// Parse the first half for the latitude coordinates
		int deg = Integer.parseInt(pcommand[0]);
		int min = Integer.parseInt(pcommand[1]);
		double sec = Double.parseDouble(pcommand[2]);

		if (!(deg >= 0 && deg <= 90) || !(min >= 0 && min < 60)
				|| !(sec >= 0 && sec < 60))
			throw new ParseException("Invalid Latitude Coordinates >" + command);

		Latitude latitude = new Latitude(deg, min, sec);

		// Parse the second half for the longitude coordinates.
		deg = Integer.parseInt(pcommand[3]);
		min = Integer.parseInt(pcommand[4]);
		sec = Double.parseDouble(pcommand[5]);

		if (!(deg >= 0 && deg <= 180) || !(min >= 0 && min < 60)
				|| !(sec >= 0 && sec < 60))
			throw new ParseException("Invalid Longitude Coordinates >"
					+ command);

		Longitude longitude = new Longitude(deg, min, sec);

		CoordinateWorld cw = new CoordinateWorld(latitude, longitude);

		return cw;
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE TRAP <aid> FROM <tid> Creates trap agent aid from trap template
	 * tid. This command populates CommandCreationalCreateTrap.
	 * 
	 * @throws ParseException
	 */
	protected void CreateTrap(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+TRAP\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateTrap(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE BARRIER <aid> FROM <tid> Creates barrier agent aid from barrier
	 * template tid. This command populates CommandCreationalCreateBarrier.
	 * 
	 * @throws ParseException
	 */
	protected void CreateBarrier(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+BARRIER\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateBarrier(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE AUX_TANK <aid> FROM <tid> Creates auxiliary-tank agent aid from
	 * auxiliary-tank template tid. This command populates
	 * CommandCreationalCreateAuxiliaryTank.
	 * 
	 * @throws ParseException
	 */
	protected void CreateAux_Tank(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+AUX_TANK\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateAuxiliaryTank(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE CATAPULT <aid> FROM <tid> Creates catapult agent aid from catapult
	 * template tid. This command populates CommandCreationalCreateCatapult.
	 * 
	 * @throws ParseException
	 */
	protected void CreateCatapult(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+CATAPULT\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateCatapult(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE OLS_XMT <aid> FROM <tid> Creates the transmitter component of
	 * optical-landing-system agent aid from optical-landing-system transmitter
	 * template tid. This command populates
	 * CommandCreationalCreateOLSTransmitter.
	 * 
	 * @throws ParseException
	 */
	protected void CreateOls_XMT(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+OLS_XMT\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateOLSTransmitter(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE OLS_RCV <aid> FROM <tid> Creates the receiver component of
	 * optical-landing-system agent aid from optical-landing-system receiver
	 * template tid. This command populates CommandCreationalCreateOLSReceiver.
	 * 
	 * @throws ParseException
	 */
	protected void CreateOls_RCV(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+OLS_RCV\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		acc.submit(new CommandCreationalCreateOLSReceiver(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE BOOM <aid> FROM <tid> Creates male or female boom agent aid from
	 * male or female boom template tid, respectively. This command populates
	 * CommandCreationalCreateBoomMale or CommandCreationalCreateBoomFemale,
	 * depending on the form of tid.
	 * 
	 * @param CommandCreationalDefineBoom
	 * @throws ParseException
	 */
	protected void CreateBoom(String input, String[] command,
			ActionCreationalCreate acc, ActionSet actionset)
			throws ParseException
	{
		String regex = "\\s*CREATE\\s+BOOM\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);

		MapTemplate mapt = actionset.getMapTemplates();
		// Typecasting?? I can't find any other reasonable way not too
		A_CommandCreationalDefineBoom ccdb = (A_CommandCreationalDefineBoom) mapt.getCommand(tid);

		if (ccdb.isMale())
			acc.submit(new CommandCreationalCreateBoomMale(aid, tid));
		else
			acc.submit(new CommandCreationalCreateBoomFemale(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * CREATE TAILHOOK <aid> FROM <tid> Creates tailhook agent aid from tailhook
	 * template tid. This command populates CommandCreationalCreateTailhook.
	 * 
	 * @throws ParseException
	 */
	protected void CreateTailHook(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*CREATE\\s+TAILHOOK\\s+\\w+\\s+FROM\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[2]);
		Identifier tid = new Identifier(command[4]);
		
		acc.submit( new CommandCreationalCreateTailhook(aid, tid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * UNCREATE <aid> Uncreates agent aid. This command populates
	 * CommandCreationalUncreate. An agent may not be uncreated if it is already
	 * part of another agent.
	 * 
	 * @throws ParseException
	 */
	protected void Uncreate(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*UNCREATE\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[1]);
		
		acc.submit( new CommandCreationalUncreate(aid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * DESCRIBE <aid> Outputs the contents of agent tid. This command populates
	 * CommandCreationalDescribe.
	 * 
	 * @throws ParseException
	 */
	protected void Describe(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*DESCRIBE\\s+\\w+\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command > " + input);

		Identifier aid = new Identifier(command[1]);
		acc.submit( new CommandCreationalDescribe(aid));
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * LIST AGENTS Outputs the identifiers of all agents. This command populates
	 * CommandCreationalListAgents.
	 * 
	 * @throws ParseException
	 */
	protected void ListAgents(String input, String[] command,
			ActionCreationalCreate acc) throws ParseException
	{
		String regex = "\\s*LIST\\s+AGENTS\\s*";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		if (!p.matcher(input).matches())
			throw new ParseException("Invalid command >" + input);

		acc.submit( new CommandCreationalListAgents());
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * getString Repieces the string array into a
	 * 
	 * @return a string piece of the current string
	 */
	/*
	 * private String getString(String[] array){ String res = ""; for( String s:
	 * array) res+= s.toString() + " "; return res; }
	 */

	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * parseString returns a string which is split into an array...the string is
	 * split by spaces any extra spaces are trimmed
	 */
	protected String[] parseString(String command)
	{
		// replace any multiple spaces with a single space
		command = command.replaceAll("\\s{2,}", " ");
		// Trim any whitespaces before and after.
		command = command.trim();
		// split on one or more spaces
		String[] res = command.split("\\s");

		// Remove any leading and trailing whitespaces
		for (int i = 0; i < res.length; i++)
			res[i] = res[i].trim();

		return res;
	}
} // end CommandParserCreationalAgents


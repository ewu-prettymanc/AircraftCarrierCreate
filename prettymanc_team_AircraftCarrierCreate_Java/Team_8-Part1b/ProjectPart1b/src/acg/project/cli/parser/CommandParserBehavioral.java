package acg.project.cli.parser;

import java.util.regex.Pattern;

import acg.architecture.datatype.Altitude;
import acg.architecture.datatype.AngleNavigational;
import acg.architecture.datatype.CoordinateWorld;
import acg.architecture.datatype.Identifier;
import acg.architecture.datatype.Latitude;
import acg.architecture.datatype.Longitude;
import acg.architecture.datatype.Speed;
import acg.project.action.ActionSet;
import acg.project.action.command.behavioral.CommandBehavioralDoAsk;
import acg.project.action.command.behavioral.CommandBehavioralDoBarrier;
import acg.project.action.command.behavioral.CommandBehavioralDoBoom;
import acg.project.action.command.behavioral.CommandBehavioralDoCaptureOLS;
import acg.project.action.command.behavioral.CommandBehavioralDoCatapult;
import acg.project.action.command.behavioral.CommandBehavioralDoForceAll;
import acg.project.action.command.behavioral.CommandBehavioralDoForceAltitude;
import acg.project.action.command.behavioral.CommandBehavioralDoForceCoordinates;
import acg.project.action.command.behavioral.CommandBehavioralDoForceHeading;
import acg.project.action.command.behavioral.CommandBehavioralDoForceSpeed;
import acg.project.action.command.behavioral.CommandBehavioralDoPosition;
import acg.project.action.command.behavioral.CommandBehavioralDoSetAltitude;
import acg.project.action.command.behavioral.CommandBehavioralDoSetHeading;
import acg.project.action.command.behavioral.CommandBehavioralDoSetSpeed;
import acg.project.action.command.behavioral.CommandBehavioralDoTailhook;
import acg.project.action.command.behavioral.CommandBehavioralDoTransfer;
import acg.project.action.command.behavioral.CommandBehavioralGetWindConditions;
import acg.project.action.command.behavioral.CommandBehavioralSetWindDirection;
import acg.project.action.command.behavioral.CommandBehavioralSetWindSpeed;

/**
 * @author Samir Ouahhabi
 * 
 */

public class CommandParserBehavioral implements I_Command
{
	protected ActionSet _actionSet;
	protected String _command;

	/**
	 * 
	 */
	@Override
	public void parseCommand(ActionSet actionset, String command)
			throws ParseException
	{
		_actionSet = actionset;
		_command = command;
		String[] line = command.split(" ");

		if (line != null && line.length >= 3)
		{
			switch (line[0].toUpperCase())
			{
			case "DO":
				parseBehavioralDo(line);
				break;
			case "@DO":
				parseBehavioralDoForce(line);
				break;
			case "SET":
				parseBehavioralSet(line);
				break;
			case "GET":
				parseBehavioralGet(line);
				break;
			default:
				throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
			}
		} else
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
	}

	/**
	 * GET�WIND�CONDITIONS
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralGet(String[] line) throws ParseException
	{
		if (!line[1].equalsIgnoreCase("wind")
				|| !line[2].equalsIgnoreCase("conditions"))
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");

		CommandBehavioralGetWindConditions command = new CommandBehavioralGetWindConditions();
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * SET�WIND�DIRECTION�<course> SET�WIND�SPEED�<speed>
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralSet(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		if (line[1].equalsIgnoreCase("wind")
				&& line[2].equalsIgnoreCase("direction"))
		{
			int courseVal = Integer.parseInt(line[3]);
			if (courseVal < 0 || courseVal > 359)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			AngleNavigational course = new AngleNavigational(courseVal);
			CommandBehavioralSetWindDirection command = new CommandBehavioralSetWindDirection(
					course);
			_actionSet.getActionBehavioral().submit(command);
		} else if (line[1].equalsIgnoreCase("wind")
				&& line[2].equalsIgnoreCase("speed"))
		{
			int speedVal = Integer.parseInt(line[3]);
			if (speedVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Speed speed = new Speed(speedVal);
			CommandBehavioralSetWindSpeed command = new CommandBehavioralSetWindSpeed(
					speed);
			_actionSet.getActionBehavioral().submit(command);
		}
	}

	/**
	 * DO FORCE
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoForce(String[] line) throws ParseException
	{
		if (line.length < 5)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		switch (line[3].toUpperCase())
		{
		case "COORDINATES":
			parseBehavioralDoForceCoordinates(line);
			break;
		case "ALTITUDE":
			parseBehavioralDoForceAltitude(line);
			break;
		case "HEADING":
			parseBehavioralDoForceHeading(line);
			break;
		case "SPEED":
			parseBehavioralDoForceSpeed(line);
			break;
		default:
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		}
	}

	/**
	 * @DO�<aid>�FORCE�COORDINATES�<coordinates>�
	 * @DO�<aid>�FORCE�COORDINATES�<coordinates>�[ALTITUDE�<altitude>]�HEADING�<course>�SPEED�<speed>
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoForceCoordinates(String[] line)
			throws ParseException
	{
		if (line.length == 5 && line[3].equalsIgnoreCase("coordinates"))
		{
			Identifier id = new Identifier(line[1]);
			CoordinateWorld coord = parseCoordinates(line[4]);
			CommandBehavioralDoForceCoordinates command = new CommandBehavioralDoForceCoordinates(
					id, coord);
			_actionSet.getActionBehavioral().submit(command);
		} else if (line.length == 9 && line[3].equalsIgnoreCase("coordinates")
				&& line[5].equalsIgnoreCase("heading")
				&& line[7].equalsIgnoreCase("speed"))
		{
			Identifier id = new Identifier(line[1]);
			CoordinateWorld coord = parseCoordinates(line[4]);
			int courseVal = Integer.parseInt(line[6]);
			if (courseVal < 0 || courseVal > 359)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			AngleNavigational course = new AngleNavigational(courseVal);
			int speedVal = Integer.parseInt(line[8]);
			if (speedVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Speed speed = new Speed(speedVal);
			CommandBehavioralDoForceAll command = new CommandBehavioralDoForceAll(
					id, coord, course, speed);
			_actionSet.getActionBehavioral().submit(command);
		} else if (line.length == 11 && line[3].equalsIgnoreCase("coordinates")
				&& line[5].equalsIgnoreCase("altitude")
				&& line[7].equalsIgnoreCase("heading")
				&& line[9].equalsIgnoreCase("speed"))
		{
			Identifier id = new Identifier(line[1]);
			CoordinateWorld coord = parseCoordinates(line[4]);
			int altitudeVal = Integer.parseInt(line[6]);
			if (altitudeVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Altitude altitude = new Altitude(altitudeVal);
			int courseVal = Integer.parseInt(line[8]);
			if (courseVal < 0 || courseVal > 359)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			AngleNavigational course = new AngleNavigational(courseVal);
			int speedVal = Integer.parseInt(line[10]);
			if (speedVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Speed speed = new Speed(speedVal);
			CommandBehavioralDoForceAll command = new CommandBehavioralDoForceAll(
					id, coord, course, speed);
			_actionSet.getActionBehavioral().submit(command);
		} else
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
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

	/**
	 * @DO�<aid>�FORCE�ALTITUDE�<altitude>
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoForceAltitude(String[] line)
			throws ParseException
	{
		Identifier id = new Identifier(line[1]);
		int altitudeVal = Integer.parseInt(line[4]);
		if (altitudeVal < 0)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Altitude altitude = new Altitude(altitudeVal);
		CommandBehavioralDoForceAltitude command = new CommandBehavioralDoForceAltitude(
				id, altitude);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * @DO�<aid>�FORCE�HEADING�<course>�
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoForceHeading(String[] line)
			throws ParseException
	{
		Identifier id = new Identifier(line[1]);
		int courseVal = Integer.parseInt(line[4]);
		if (courseVal < 0 || courseVal > 359)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		AngleNavigational course = new AngleNavigational(courseVal);
		CommandBehavioralDoForceHeading command = new CommandBehavioralDoForceHeading(
				id, course);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * @DO�<aid>�FORCE�SPEED�<speed>
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoForceSpeed(String[] line)
			throws ParseException
	{
		Identifier id = new Identifier(line[1]);
		int speedVal = Integer.parseInt(line[4]);
		if (speedVal < 0)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Speed speed = new Speed(speedVal);
		CommandBehavioralDoForceSpeed command = new CommandBehavioralDoForceSpeed(
				id, speed);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDo(String[] line) throws ParseException
	{
		switch (line[2].toUpperCase())
		{
		case "ASK":
			parseBehavioralDoAsk(line);
			break;
		case "POSITION":
			parseBehavioralDoPosition(line);
			break;
		case "BARRIER":
			parseBehavioralDoBarrier(line);
			break;
		case "CATAPULT":
			parseBehavioralDoCatapult(line);
			break;
		case "SET":
			parseBehavioralDoSet(line);
			break;
		case "TAILHOOK":
			parseBehavioralDoTailhook(line);
			break;
		case "CAPTURE":
			parseBehavioralDoCapture(line);
			break;
		case "BOOM":
			parseBehavioralDoBoom(line);
			break;
		case "TRANSFER":
			parseBehavioralDoTransfer(line);
			break;
		default:
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		}
	}

	/**
	 * DO�<aid>�ASK�ALL|COORDINATES|ALTITUDE|HEADING|SPEED|WEIGHT|FUEL
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoAsk(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoAsk command = new CommandBehavioralDoAsk(id,
				CommandBehavioralDoAsk.E_Parameter.valueOf(line[3]
						.toUpperCase()));
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�POSITION
	 * 
	 * @param line
	 */
	private void parseBehavioralDoPosition(String[] line)
	{
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoPosition command = new CommandBehavioralDoPosition(
				id);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�BARRIER�(UP|DOWN)
	 * 
	 * @param line
	 */
	private void parseBehavioralDoBarrier(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoBarrier command = new CommandBehavioralDoBarrier(id,
				line[3].equalsIgnoreCase("UP"));
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�CATAPULT�LAUNCH�WITH�SPEED�<speed>
	 * 
	 * @param line
	 */
	private void parseBehavioralDoCatapult(String[] line) throws ParseException
	{
		if (line.length < 7)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		int speedVal = Integer.parseInt(line[6]);
		if (speedVal < 0)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Speed speed = new Speed(speedVal);
		CommandBehavioralDoCatapult command = new CommandBehavioralDoCatapult(
				id, speed);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�SET�SPEED�<speed>
	 * DO�<aid>�SET�ALTITUDE�<altitude>
	 * DO�<aid>�SET�HEADING�<course>�[LEFT|RIGHT]
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoSet(String[] line) throws ParseException
	{
		if (line.length < 5)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		switch (line[3].toUpperCase())
		{
		case "SPEED":
			int speedVal = Integer.parseInt(line[4]);
			if (speedVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Speed speed = new Speed(speedVal);
			CommandBehavioralDoSetSpeed command = new CommandBehavioralDoSetSpeed(
					id, speed);
			_actionSet.getActionBehavioral().submit(command);
			break;
		case "ALTITUDE":
			int altitudeVal = Integer.parseInt(line[4]);
			if (altitudeVal < 0)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			Altitude altitude = new Altitude(altitudeVal);
			CommandBehavioralDoSetAltitude command2 = new CommandBehavioralDoSetAltitude(
					id, altitude);
			_actionSet.getActionBehavioral().submit(command2);
			break;
		case "HEADING":
			if (line.length < 6)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			int courseVal = Integer.parseInt(line[4]);
			if (courseVal < 0 || courseVal > 359)
				throw new ParseException("\"" + _command
						+ "\" is an invalid Behavioral command");
			AngleNavigational course = new AngleNavigational(courseVal);
			CommandBehavioralDoSetHeading command3 = new CommandBehavioralDoSetHeading(
					id, course,
					CommandBehavioralDoSetHeading.E_Direction.valueOf(line[5]
							.toUpperCase()));
			_actionSet.getActionBehavioral().submit(command3);
			break;
		default:
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		}
	}

	/**
	 * DO�<aid>�TAILHOOK�(UP|DOWN)
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoTailhook(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoTailhook command = new CommandBehavioralDoTailhook(
				id, line[3].equalsIgnoreCase("UP"));
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�CAPTURE�OLS
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoCapture(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoCaptureOLS command = new CommandBehavioralDoCaptureOLS(
				id);
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�BOOM�(EXTEND|RETRACT)
	 * 
	 * @param line
	 */
	private void parseBehavioralDoBoom(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoBoom command = new CommandBehavioralDoBoom(id,
				line[3].equalsIgnoreCase("EXTEND"));
		_actionSet.getActionBehavioral().submit(command);
	}

	/**
	 * DO�<aid>�TRANSFER�(START|STOP)
	 * 
	 * @param line
	 * @throws ParseException
	 */
	private void parseBehavioralDoTransfer(String[] line) throws ParseException
	{
		if (line.length < 4)
			throw new ParseException("\"" + _command
					+ "\" is an invalid Behavioral command");
		Identifier id = new Identifier(line[1]);
		CommandBehavioralDoTransfer command = new CommandBehavioralDoTransfer(
				id, line[3].equalsIgnoreCase("START"));
		_actionSet.getActionBehavioral().submit(command);
	}

}

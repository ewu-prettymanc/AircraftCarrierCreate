package acg.project.cli.parser;

import acg.project.action.ActionSet;

/**
 * 
 * @author Samir Ouahhabi
 * 
 */

public class CommandParser
{
	protected ActionSet _actionSet;
	protected String _command;

	/**
	 * Constructor
	 * 
	 * @param actionSet
	 * @param command
	 */
	public CommandParser(ActionSet actionSet, String command)
	{
		_actionSet = actionSet;
		_command = command;
	}

	/**
	 * Interprets the input from the constructor and delegates the appropriate
	 * actions for execution.
	 * 
	 * @throws ParseException
	 */
	public void interpret() throws ParseException
	{
		// checking for null or empty command
		if (_command.isEmpty() || _command.equals(""))
			throw new ParseException("\"" + _command
					+ "\" is an invalid command");
		String[] pieces = _command.split("//");

		// handling comments
		if (pieces.length == 0 || pieces[0].equals(""))
			return;

		String command = pieces[0];
		pieces = command.split(";");
		for (String s : pieces)
		{
			transmitCommand(s);
		}
	}

	private void transmitCommand(String s) throws ParseException
	{
		String command = s.trim();
		I_Command cmd;
		;
		if (!command.isEmpty() && command != null)
		{
			String[] pieces = command.split(" ");
			if (pieces[0].equalsIgnoreCase("define")
					|| pieces[0].equalsIgnoreCase("undefine")
					|| pieces[0].equalsIgnoreCase("show"))
			{
				// templates
				cmd = new CommandParserCreationalTemplate();
				cmd.parseCommand(_actionSet, command);
			} else if (pieces[0].equalsIgnoreCase("create")
					|| pieces[0].equalsIgnoreCase("uncreate")
					|| pieces[0].equalsIgnoreCase("describe"))
			{
				// agents
				cmd = new CommandParserCreationalAgents();
				cmd.parseCommand(_actionSet, command);
			} else if (pieces[0].equalsIgnoreCase("list"))
			{
				// list
				if (pieces.length < 2)
					throw new ParseException("\"" + _command
							+ "\" is an invalid command");
				if (pieces[1].equalsIgnoreCase("templates"))
				{
					cmd = new CommandParserCreationalTemplate();
					cmd.parseCommand(_actionSet, command);
				} else if (pieces[1].equalsIgnoreCase("agents"))
				{
					cmd = new CommandParserCreationalAgents();
					cmd.parseCommand(_actionSet, command);
				} else
					throw new ParseException("\"" + _command
							+ "\" is an invalid command");
			} else if (pieces[0].equalsIgnoreCase("populate")
					|| pieces[0].equalsIgnoreCase("commit"))
			{
				// structural
				cmd = new CommandParserStructural();
				cmd.parseCommand(_actionSet, command);
			} else if (pieces[0].equalsIgnoreCase("@clock")
					|| pieces[0].equalsIgnoreCase("@run")
					|| pieces[0].equalsIgnoreCase("@exit")
					|| pieces[0].equalsIgnoreCase("@wait"))
			{
				// misc
				cmd = new CommandParserMisc();
				cmd.parseCommand(_actionSet, command);
			} else if (pieces[0].equalsIgnoreCase("do")
					|| pieces[0].equalsIgnoreCase("@do")
					|| pieces[0].equalsIgnoreCase("get")
					|| pieces[0].equalsIgnoreCase("set"))
			{
				// behavioral
				cmd = new CommandParserBehavioral();
				cmd.parseCommand(_actionSet, command);
			}
		}
	}
}

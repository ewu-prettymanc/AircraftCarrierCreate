package acg.project.cli.parser;

import acg.project.action.ActionSet;

/**
 * 
 * @author Samir Ouahhabi
 *
 */
public interface I_Command
{
	/**
	 * 
	 * @param actionset
	 * @param command
	 * @throws ParseException
	 */
	public void parseCommand(ActionSet actionset, String command) throws ParseException;
}

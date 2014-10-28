package acg.project.cli.parser;

import acg.project.action.ActionSet;
import acg.project.action.ActionStructural;
import acg.project.action.command.structural.*;
import acg.project.cli.parser.ParseException;
import acg.architecture.datatype.*;
import java.util.*;


/**
 * This class is dedicated to the parsing of structural commands.
 * If the class encounters unexpected syntax while parsing,
 * it throws a ParseException.
 * 
 * @author Evan Nilson
 */
public class CommandParserStructural implements I_Command
{

	protected ActionSet _actionset;
	
	protected String _command;
	
	
	/**
	 * Parses a command, and passes it to the appropriate method.
	 * Implemented from interface I_Command.
	 * 
	 * @throws ParseException
	 */
	public void parseCommand(ActionSet actionset, String command) throws ParseException
	{
	
	_actionset = actionset;
	_command = command;
	
	String[] syntax = command.split(" ");
	
	for(String s : syntax)
	{
	s.trim(); //clean up extra whitespaces
	}//end loop

	String firstCommand = syntax[0].toUpperCase();
	
	switch (firstCommand)
	{
		case "POPULATE":
			String secondCommand = syntax[1].toUpperCase();
			switch (secondCommand)
			{
				case "CARRIER":
					populateCarrier(syntax);
					break;
				case "WORLD":
					populateWorld(syntax);
					break;
			}//end switch
			break;
		case "COMMIT":
			commit();
			break;
		default:
			throw new ParseException("Invalid Command > " + command);
	}//end switch
	}//end parseCommand
	
	
	/**
	 * POPULATE CARRIER <aid1> WITH FIGHTER[S] <aidn>+
	 * 
	 * Populates carrier agent aid1 with fighter agents aidn.
	 * The syntax of FIGHTER need not correspond to the number of identifiers.
	 * Only fighters created without an initial airborne state may be added.
	 * This command populates CommandStructuralPopulateCarrier.
	 * @throws ParseException
	 */
	protected void populateCarrier(String[] desc) throws ParseException
	{
	if(desc.length < 6 || !desc[3].equalsIgnoreCase("WITH") || !(desc[4].equalsIgnoreCase("FIGHTERS") || desc[4].equalsIgnoreCase("FIGHTER")))
		throw new ParseException("Invalid Command > " + getString(desc));
	
	ActionStructural actStruct = _actionset.getActionStructural();
	
	Identifier carrierID = new Identifier(desc[2]);
	List<Identifier> fighterIDs = new ArrayList<Identifier>();
	for(int i = 5; i < desc.length && !desc[i].startsWith("//"); i++)
	{
		fighterIDs.add(new Identifier(desc[i]));
	}
	
	CommandStructuralPopulateCarrier popCarrier = new CommandStructuralPopulateCarrier(carrierID, fighterIDs);
	actStruct.submit(popCarrier);
	}//end method
	
	/**
	 * POPULATE WORLD WITH <aidn>+
	 * 
	 * Populates the world with fighter, tanker, and carrier agents aidn.
	 * This command populates CommandStructuralPopulateWorld.
	 * @throws ParseException
	 */
	protected void populateWorld(String[] desc) throws ParseException
	{
	if(desc.length < 4 || !desc[2].equalsIgnoreCase("WITH"))
		throw new ParseException("Invalid Command > " + getString(desc));
	
		ActionStructural actStruct = _actionset.getActionStructural();
	
	List<Identifier> idList = new ArrayList<Identifier>();
	
	for(int i = 3; i < desc.length && !desc[i].startsWith("//"); i++)
	{
		idList.add(new Identifier(desc[i]));
	}
	
	CommandStructuralPopulateWorld popWorld = new CommandStructuralPopulateWorld(idList);
	actStruct.submit(popWorld);
	}//end method
	
	
	/**
	 * COMMIT
	 * 
	 * Locks the membership in the world. No creational or structural commands are allowed after this point.
	 * This command populates CommandStructuralCommit.
	 * 
	 */
	protected void commit()
	{
		ActionStructural actStruct = _actionset.getActionStructural();
		CommandStructuralCommit commit = new CommandStructuralCommit();
		actStruct.submit(commit);
	}
	
	public String getString(String[] desc)
	{
		String s = "";
		for(String word : desc)
		{
			s += (word + " ");
		}
		return s;
	}
}//class
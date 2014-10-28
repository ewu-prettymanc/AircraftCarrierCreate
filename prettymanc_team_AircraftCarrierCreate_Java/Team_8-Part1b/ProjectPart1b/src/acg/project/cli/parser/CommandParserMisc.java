package acg.project.cli.parser;

import java.io.File;
import java.util.regex.Pattern;

import acg.architecture.datatype.Rate;
import acg.project.action.ActionMiscellaneous;
import acg.project.action.ActionSet;
import acg.project.action.command.miscellaneous.CommandMiscDoClockUpdate;
import acg.project.action.command.miscellaneous.CommandMiscDoExit;
import acg.project.action.command.miscellaneous.CommandMiscDoRun;
import acg.project.action.command.miscellaneous.CommandMiscDoSetClockRate;
import acg.project.action.command.miscellaneous.CommandMiscDoSetClockRunning;
import acg.project.action.command.miscellaneous.CommandMiscDoShowClock;
import acg.project.action.command.miscellaneous.CommandMiscDoWait;

//==================================================================================================================================================================================
/**
 * Miscellaneous commands control the execution of the simulation.
 * The contents of these commands populate a command object in acg.project.action.command.miscellaneous and
 * submit it to ActionSet::getActionMisc().submit() from your parser.
 * @author Colton Prettyman
 */
public class CommandParserMisc implements I_Command{

	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * This override method takes an Actionset and String command as its parameter and 
	 * switches on input to parse the command and create the correct agent.
	 * @param actionset - The actionset
	 * @param command - The input string containing the command information. 
	 * @throws ParseException
	 */
	@Override
	public void parseCommand(ActionSet actionset, String command)
			throws ParseException {
		
	ActionMiscellaneous actionmisc = actionset.getActionMisc();
		
		
		String[] pcommand = parseString(command);
		
		if( pcommand[0].equalsIgnoreCase("@Clock")) {
			
			// Check if just @CLOCK
			String regex = "\\s*@CLOCK\\s*";
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			boolean done = false;
			if( done = p.matcher(command).matches() ) 
				outputClock(command,pcommand,actionmisc);
			
			//Check if PAUSE|RESUME|UPDATE
			regex = "\\s*@CLOCK\\s+(PAUSE|RESUME|UPDATE)\\s*";
		    p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			if( !done && (done = p.matcher(command).matches()) )
				instructClock(command,pcommand,actionmisc);
			
			//Check if <rate>
			regex = "\\s*@CLOCK\\s+\\d+\\s*";
		    p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			if( !done && (done = p.matcher(command).matches()) )
				setClock(command,pcommand,actionmisc);
			
			// Bad command!!
			if( ! done )
			throw new ParseException("Invalid command > " + command);
		}// end if
		else 
		{
			switch(pcommand[0].toUpperCase() ) {
			
				case "@RUN" : run(command, pcommand, actionmisc);
					break;
				
				case "@EXIT" : exit(actionmisc);
					break;
				
				case "@WAIT" : wait(command, pcommand, actionmisc);
					break;
				
				default : throw new ParseException("Invalid command > " + command);
			}
		} // end else
	}

	/**
	 * @CLOCK
	 * Outputs the clock rate to the command-line interface as “clock = <rate>”, or “clock = paused” if it is not running.
	 * This command populates CommandMiscDoShowClock.
	 * @param command
	 * @param pcommand
	 * @param actionmisc
	 */
		protected void outputClock(String command, String[] pcommand, ActionMiscellaneous actionmisc) {
		actionmisc.submit(new CommandMiscDoShowClock() );
	}
		
	/**
	 * @CLOCK PAUSE|RESUME|UPDATE
	 * Instructs the simulation to pause or resume the system clock, respectively, or to force it to advance a tick. Force is valid only when the
	 * clock is paused.
	 * This command populates CommandDoSetClockRunning or CommandDoClockUpdate.
	 * @param command
	 * @param pcommand
	 * @param actionmisc
	 * @throws ParseException 
	 */
		protected void instructClock(String command, String[] pcommand, ActionMiscellaneous actionmisc) throws ParseException {
		
			switch( pcommand[1].toUpperCase() ) {
			
				case "PAUSE" :  actionmisc.submit(new CommandMiscDoSetClockRunning(false));
					break;
				case "RESUME" : actionmisc.submit( new CommandMiscDoSetClockRunning(true));
					break;
				case "UPDATE" : actionmisc.submit( new CommandMiscDoClockUpdate());
					break;
				default : throw new ParseException("Invalid command > " + command );
			}
	}

	/**
	 * @CLOCK <rate>
	 * Sets the system clock speed to rate ticks per second.
	 * This command populates CommandDoSetClockRate.
	 * @param command
	 * @param pcommand
	 * @param actionmisc
	 * @throws ParseException 
	 */
		protected void setClock(String command, String[] pcommand, ActionMiscellaneous actionmisc) throws ParseException {
		int rate = Integer.parseInt(pcommand[1]);
		if( rate < 0 )
			throw new ParseException("Invalid input > " + command);
		
		actionmisc.submit( new CommandMiscDoSetClockRate( new Rate(rate)) ) ;
	}

	/**
	 * @RUN <string>
	 * Loads a text file with commands of the form here, one per line, and executes them in order. string is any filename with path and 
	 * extension.This command populates CommandMiscDoRun.
	 * @param command
	 * @param pcommand
	 * @param actionmisc
	 * @throws ParseException 
	 */
		protected void run(String command, String[] pcommand, ActionMiscellaneous actionmisc) throws ParseException {
	   if(! new File( pcommand[1]).isFile() )
		   throw new ParseException("Invalid filename > " + command);
		
		actionmisc.submit( new CommandMiscDoRun(pcommand[1]));
	}
		
	/**
	 * @EXIT
	 * Exits the system.
	 * This command populates CommandMiscDoExit.
	 * @param actionmisc
	 */
		protected void exit( ActionMiscellaneous actionmisc) {
			actionmisc.submit(new CommandMiscDoExit() );
	}

	/**
	 * @WAIT <rate>
	 * Waits rate ticks before executing the next behavioral command. This command is not valid until after COMMIT.
	 * This command populates CommandMiscDoWait.
	 * @param command
	 * @param pcommand
	 * @param actionmisc
	 * @throws ParseException 
	 */
		protected void wait(String command, String[] pcommand, ActionMiscellaneous actionmisc) throws ParseException {
			String regex = "\\s*@WAIT\\s+\\d+\\s*";
			Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			if( ! p.matcher(command).matches() )
				throw new ParseException("Invalid input > " + command);
			
			int rate = Integer.parseInt(pcommand[1]);
			
			if( rate < 0)
				throw new ParseException("Invalid rate > " + pcommand[1]);
			
			actionmisc.submit(new CommandMiscDoWait( new Rate( rate ) ));
	}

		//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		/**
		 * parseString -- This method is duplicated in other command parser class. 
		 * Maybe an inheretance heirarchy would'be been better then an interface.
		 * returns a string which is split into an array...the string is split by spaces
		 * any extra spaces are trimmed
		 */
		protected String[] parseString(String command){
			// replace any multiple spaces with a single space
			command = command.replaceAll("\\s{2,}", " ");
			// Trim any whitespaces before and after.
			command = command.trim();
			// split on one or more spaces
			String[] res = command.split("\\s");
			
			// Remove any leading and trailing whitespaces
			for( int i=0; i < res.length; i++)
				res[i] = res[i].trim();
			
			return res;
		}

}

package acg.project.cli.parser;

import java.util.Scanner;

import acg.project.action.ActionSet;
import acg.project.cli.CommandLineInterface;
import acg.project.cli.parser.ParseException;

public class Test
{
	public static void main(String[] args) throws ParseException
	{
		ActionSet actionset = new ActionSet(new CommandLineInterface());
		CommandParser cp;
		Scanner scan = new Scanner(System.in);
		String input = "";
		while (!input.equals("-1"))
		{
			try
			{
				System.out.print("> ");
				input = scan.nextLine();
				cp = new CommandParser(actionset, input);
				cp.interpret();
				Thread.sleep(100);
			} catch (Exception e)
			{
				System.out.println(e);
			}
		}
	}
}

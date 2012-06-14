import MySQLdb as ms
import matplotlib.pyplot as plt
from numpy import *
from pylab import *

def main():
    # Create a connection object
 conn = ms.connect(host='localhost', user='vaisagh', passwd='vaisaghviswanathan')
  
    # create the plot
   
   # Now switch to a more OO interface to exercise more features.
 fig, axs = plt.subplots(nrows=1, ncols=1, sharex=False, sharey=False)
 survivedPlot(conn,axs)
 # lastStartPlot(conn)
 # completionPlot(conn)
 plt.show()
 fig, axs = plt.subplots(nrows=1, ncols=1, sharex=False, sharey=False)
 betterStartPlot(conn,axs)
 plt.show()

 fig.suptitle('Experiment Results')
 

 plt.show()
 # close the connection, AND finish
 conn.close()


def survivedPlot(conn, ax):
 
    # Execute the query AND get the result set into a rowset
 sql = '''SELECT T1.description, avg(T1.percentageSurvived), std(T1.percentageSurvived) 
			FROM
				(SELECT asum.run_id, (sum(survived)/count(*))*100 as percentageSurvived,rinf.description
				    FROM ibevac_database.agent_summary as asum, ibevac_database.run_info as rinf 
				    WHERE asum.run_id in 
				    (   SELECT run_id 
				        FROM ibevac_database.run_info 
				        WHERE experiment_id=1) 
				    AND asum.run_id=rinf.run_id 
				    GROUP BY run_id) as T1
				    GROUP BY T1.description
				    ORDER BY abs(T1.description);'''
 plotWithData(ax,sql, conn)

 sql = '''SELECT T1.description, avg(T1.percentageSurvived), std(T1.percentageSurvived) 
			FROM
			(SELECT asum.run_id, (sum(survived)/count(*))*100 as percentageSurvived,count(*), rinf.numSurvived , rinf.description
			    FROM ibevac_database.agent_summary as asum, ibevac_database.run_info as rinf 
			    WHERE asum.run_id in 
			    (   SELECT run_id 
			        FROM ibevac_database.run_info 
			        WHERE experiment_id=2) 
			    AND room_id in 
			    (   SELECT area_id 
			        FROM ibevac_database.area_info 
			        WHERE floor=0) 
			    AND asum.run_id=rinf.run_id 
			    GROUP BY run_id) as T1
			    GROUP BY T1.description
			    ORDER BY abs(T1.description);'''
 plotWithData(ax,sql, conn)
 
 
 ax.set_title("Survival Rate")
 ax.set_xlabel('Ambiguity');
 ax.set_ylabel('Survival Percentage');
 ax.set_ybound(lower=0, upper=100)
 ax.set_xbound(lower=0.0, upper= 1.0)
 # ax.set_grid(True);
 leg = ax.legend(('Experiment 1', 'Experiment 2'),
           'lower right', shadow=False)
 # set some legend properties.  All the code below is optional.  The
 # defaults are usually sensible but if you need more control, this
 # shows you how
 
 # the matplotlib.patches.Rectangle instance surrounding the legend
 frame  = leg.get_frame()
 frame.set_facecolor('0.80')    # set the frame face color to light gray
 
 # matplotlib.text.Text instances
 for t in leg.get_texts():
     t.set_fontsize('small')    # the legend text fontsize
 
 # matplotlib.lines.Line2D instances
 for l in leg.get_lines():
  l.set_linewidth(1.5)  # the legend line width

   

def betterStartPlot(conn, ax):
 # plt.figure()
 sql = "SELECT description,avg(maxStart), std(maxStart) FROM ibevac_database.run_info WHERE experiment_id=1 GROUP BY description order by abs(description)"
 plotWithData(ax,sql, conn)
 # plt.title("Average number survived for each setting")
 # plt.show()	

 sql = '''SELECT T1.description, avg(T1.actStart), std(T1.actStart) 
			FROM
			(   SELECT asum.run_id, max(evac_start_time) as actStart, rinf.maxStart , rinf.description
			    FROM ibevac_database.agent_summary as asum, ibevac_database.run_info as rinf 
			    WHERE asum.run_id in 
			    (   SELECT run_id 
			        FROM ibevac_database.run_info 
			        WHERE experiment_id=2) 
			    AND room_id in 
			    (   SELECT area_id 
			        FROM ibevac_database.area_info 
			        WHERE floor=0) 
			    AND asum.run_id=rinf.run_id 
			    GROUP BY run_id) as T1
			    GROUP BY T1.description order by abs(T1.description);'''
 plotWithData(ax,sql, conn)
 
 
 # plt.set_yticklabels([])
 # plt.set_xticklabels([])
 ax.set_title("Latest Evacuation Start Time")
 ax.set_xlabel('Ambiguity');
 ax.set_ylabel('Time Step ');
 ax.set_ybound(lower=0, upper=6000)
 ax.set_xbound(lower=0.0, upper= 1.0)
 ax.axhline(y=5820, color='g')
 ax.annotate(xy=(0.05,5650), s='average completion time', size= 'x-small')
 # ax.set_grid(True);
 
 leg = ax.legend(('Experiment 1', 'Experiment 2'),
           'lower right', shadow=False)
 # set some legend properties.  All the code below is optional.  The
 # defaults are usually sensible but if you need more control, this
 # shows you how
 
 # the matplotlib.patches.Rectangle instance surrounding the legend
 frame  = leg.get_frame()
 frame.set_facecolor('0.80')    # set the frame face color to light gray
 
 # matplotlib.text.Text instances
 for t in leg.get_texts():
     t.set_fontsize('small')    # the legend text fontsize
 
 # matplotlib.lines.Line2D instances
 for l in leg.get_lines():
  l.set_linewidth(1.5)  # the legend line width

def plotWithData(ax, sql, conn):
 cursor = conn.cursor()
 cursor.execute(sql)
 rowset = cursor.fetchall()
 cursor.close()        
        
 # Put the x AND y values into their own arrays
 cx = []
 cy = []

 cx_err = []
 for row in rowset:
  cx.append(float(row[1]))
  cy.append(float(row[0])/10)
  cx_err.append(float(row[2]))
 
 a= array(cy)
 b= array(cx)
 err = array(cx_err)
 
 # subplot(111)
 ax.errorbar(a, b, yerr=err)

if __name__ == "__main__":
    main()






    
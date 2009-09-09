function swapDisplay(varID)
{
	itemToSwap = document.all[varID]
	if(itemToSwap.style.display=="none")
	{
		itemToSwap.style.display="inline"
		//CloseAllButOne(varID)
	}
	else
	{
		itemToSwap.style.display="none"
	}
}

function ShowMilestone(varID,numofsolutions)
{

      //alert(varID)
      //var inp = document.all[numofsolutions];
/*
	  solcount = numofsolutions;
      for(i=1;i<=solcount; i++)
      {
            var controlName = "viewDetail"+i;
            var itemClicked = document.getElementById(varID);

            itemToSwap = document.getElementById(controlName);

            //alert(itemToSwap)
            if(controlName != varID)
            {
                  itemToSwap.style.display="none"
            }
            else {
				itemToSwap.style.display="block";
            }
      }     
*/

			solcount = numofsolutions;
            var itemClicked = document.getElementById(varID);
			var controlName = "";

			  for(i=1;i<=solcount; i++)
			  {
		            controlName = "viewDetail"+i;
//			try {
					itemToSwap = document.getElementById(controlName);

					//alert("i:" + i + "solcount:" + solcount + "\n" + varID + "!=" + controlName);

					//alert(itemToSwap)
					if(controlName != varID)
					{
						itemToSwap.style.display="none";
						//document.getElementById("ms_img_"+i).src="/library/media/1031/germany/newsletter/images/"+i+"_off.gif";
						document.getElementById("ms_img_"+i).src = eval("ImgNav" + i + ".src");
					}
					else {
						itemToSwap.style.display="block";
						//document.getElementById("ms_img_"+i).src="/library/media/1031/germany/newsletter/images/"+i+"_on.gif";
						document.getElementById("ms_img_"+i).src = eval("ImgNav" + i + "a.src");
					}

//				} catch (e) {
//					alert(e);
//				}
			  }
}


function CloseAllButOne(varID,numofsolutions)
{

     // alert(varID)
      //var inp = document.all[numofsolutions];
      solcount = numofsolutions;
      for(i=1;i<=solcount; i++)

      {
            var controlName = "viewDetail"+i;

            //alert(controlName)
            var itemClicked = document.all[varID]

            itemToSwap = document.all[controlName]
            //alert(itemToSwap)
            if(controlName != varID)

            {
                  itemToSwap.style.display="none"
            }
            else {
				if(itemToSwap.style.display=="none")
				{
					itemToSwap.style.display="inline"
					//CloseAllButOne(varID)
				}
				else
				{
					itemToSwap.style.display="none"
				}
				
            }
      }     

}


        function showDetails(fid) {
        var detail = 'viewDetail'+fid;
        CloseAllButOne(detail,6);
        }

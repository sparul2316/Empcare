

let sidebar = document.querySelector(".sidebar");
let content = document.querySelector(".content");

let visible = true;

console.log("hey");

const toggleSideBar=()=>{
	
	if(visible){
		
		sidebar.style.display = "none";
		content.style.marginLeft = "5%";
		visible= false;
	
		
	}else{
		
		sidebar.style.display="block";
		content.style.marginLeft = "20%";
		visible=true;
	}
};
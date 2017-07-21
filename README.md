# SDCardManager
外置SD/TF卡容量检查工具。并且可指定SD卡剩余最小容量和需要删除的首要文件夹
串行线程。操作安全

使用方法new StorageThread("video",500).start;

	*第一个参数"String"是SD卡上指定文件(文件夹)的名字
	*第二个参数"int"意为清理到SD卡至少多大停止。单位为MB
	

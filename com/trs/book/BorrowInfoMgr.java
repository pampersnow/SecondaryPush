package com.trs.book;

import com.trs.DreamFactory;
import com.trs.infra.persistent.WCMFilter;

public class BorrowInfoMgr implements IBorrowInfoMgr {

	private IBookMgr bookMgr = (IBookMgr) DreamFactory
			.createObjectById("bookMgr");

	public void borrowBook(String sReader, Book book) {
		// TODO Auto-generated method stub

	}

	public void returnBook(String sReader, Book book) {
		// TODO Auto-generated method stub

	}

	public BorrowInfos queryBorrowInfos(Book book, WCMFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public Books queryBooks(String sReader, WCMFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

}

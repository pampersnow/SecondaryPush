package com.trs.book;

import com.trs.infra.persistent.WCMFilter;

public interface IBorrowInfoMgr {

	/**
	 * ����reader����ͼ��book
	 * 
	 * @param sReader
	 * @param book
	 */
	public void borrowBook(String sReader, Book book);

	/**
	 * �û�sReader����book
	 * 
	 * @param sReader
	 * @param book
	 */
	public void returnBook(String sReader, Book book);

	/**
	 * 
	 * @param book
	 * @param filter
	 * @return
	 */
	public BorrowInfos queryBorrowInfos(Book book, WCMFilter filter);

	/**
	 * 
	 * @param sReader
	 * @param filter
	 * @return
	 */
	public Books queryBooks(String sReader, WCMFilter filter);
}

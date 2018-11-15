package com.trs.book;

import com.trs.infra.persistent.WCMFilter;

public interface IBorrowInfoMgr {

	/**
	 * 读者reader借阅图书book
	 * 
	 * @param sReader
	 * @param book
	 */
	public void borrowBook(String sReader, Book book);

	/**
	 * 用户sReader还书book
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

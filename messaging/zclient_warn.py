from zmessaging import ZMessaging


def main():
    client = ZMessaging()
    msg = client.send_warning("test2", 5000)
    print(msg)


if __name__ == '__main__':
    main()
